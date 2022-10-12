package com.walkertribe.artemisdisplay;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.Timer;

import com.walkertribe.artemisdisplay.Configuration.RenderOption;
import com.walkertribe.artemisdisplay.display.Display;
import com.walkertribe.artemisdisplay.i18n.LocaleData;
import com.walkertribe.ian.iface.ArtemisNetworkInterface;
import com.walkertribe.ian.iface.ConnectionSuccessEvent;
import com.walkertribe.ian.iface.DisconnectEvent;
import com.walkertribe.ian.iface.Listener;
import com.walkertribe.ian.protocol.core.GameOverPacket;
import com.walkertribe.ian.protocol.core.PausePacket;
import com.walkertribe.ian.protocol.core.PlayerShipDamagePacket;
import com.walkertribe.ian.protocol.core.helm.JumpEndPacket;
import com.walkertribe.ian.protocol.core.setup.AllShipSettingsPacket;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * The surface on which the Displays are rendered. Before connecting to a server, the Canvas will
 * show "Waiting for [server]." Once connected, it will show "[ship name] standing by", where
 * [ship name] is the name of the selected ship, if known, or "Ship [index + 1]" if not.
 * 
 * This class also handles interface screws when the ship is impacted, and showing a "Paused"
 * overlay when the simulation is paused.
 * 
 * @author rjwut
 */
public class Canvas extends JComponent {
  public static final Font MONOSPACE_FONT;

  private static final Color SCRIM_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.75f);
  private static final long PULSE_INTERVAL = 2000;
  private static final long IMPACT_FADEOUT_MS = 1000;
  private static final float SHAKE_MAGNITUDE = 0.03f;
  private static final float MAX_IMPACT_STRENGTH = 0.3f;
  private static final int STATIC_GRANULARITY = 2;
  private static final long JUMP_EFFECT_DURATION_MS = 8000;
  private static final String MONOSPACE_FONT_FILE = "/DroidSansMono.ttf";

  static {
    try {
      MONOSPACE_FONT = Font.createFont(Font.TRUETYPE_FONT, Canvas.class.getResourceAsStream(MONOSPACE_FONT_FILE))
          .deriveFont(LocaleData.BASE_FONT_SIZE);
    } catch (FontFormatException | IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static final long serialVersionUID = -3218529373545600263L;

  /**
   * Returns a new TextFitter for the requested font type.
   */
  public static TextFitter getTextFitter(boolean monospace) {
    Font font = monospace ? Canvas.MONOSPACE_FONT : LocaleData.get().getFont();
    return new TextFitter(font).lineHeight(0.8f);
  }

  /**
   * Given a base Color (presumed to be full opacity), returns a new Color with the alpha channel
   * set to a value appropriate for the current time in a "pulsing" animation. Over time, the
   * resulting Colors will pulse between minAlpha and full opacity.
   */
  public static Color pulseColor(Color color, long intervalMs, float minAlpha) {
    float perc = (float) (System.currentTimeMillis() % intervalMs) / intervalMs;
    float alphaRange = 1 - minAlpha;
    float alpha;

    if (perc < 0.5) {
      alpha = alphaRange * perc * 2 + minAlpha;
    } else {
      alpha = (1 - (perc * 2 - 1)) * alphaRange + minAlpha;
    }

    return new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
  }

  private Configuration config;
  private LocaleData localeData;
  private Map<RenderingHints.Key, Object> renderingHints = new HashMap<>();
  private Timer drawTimer;
  private ArtemisNetworkInterface iface;
  private byte shipIndex = 0;
  private int shipId = -1;
  private Display display;
  private TextFitter textFitter;
  private String instructions;
  private boolean connected;
  private CharSequence shipName;
  private boolean alive;
  private boolean paused;
  private long impactEndTime;
  private long jumpEndTime;

  /**
   * Creates a new Canvas on which the given Display will be rendered.
   */
  Canvas(ArtemisDisplay app) {
    super();
    config = app.getConfig();
    localeData = LocaleData.get();
    textFitter = getTextFitter(false).padding(0.1f);

    if (config.getRenderOption(RenderOption.ANTIALIASING)) {
      renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    if (config.getRenderOption(RenderOption.SUBPIXEL_FONT_RENDERING)) {
      renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    setOpaque(true);
    display = config.buildDisplay(app);

    // If we're full screen, retrieve the instructions on how to move and close the display.
    if (config.getWindowMode() != WindowMode.WINDOWED) {
      int monitorCount = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
      String instructionsKey = "canvas.close_" + (monitorCount == 1 ? "" : "and_move_") + "instructions";
      instructions = localeData.string(instructionsKey);
    }

    // Set up animation timer
    drawTimer = new Timer(50, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        repaint();
      }
    });
  }

  /**
   * Attached an ArtemisNetworkInterface to this Canvas. This will cause it to start responding to
   * events from the server.
   */
  void attach(ArtemisNetworkInterface iface) {
    if (this.iface == iface) {
      return;
    }

    if (iface != null) {
      iface.addListener(this);
    }

    if (display != null) {
      display.attach(iface);
    }
  }

  /**
   * Invoked when the connection is established. Causes the display to change from
   * "Waiting for server" to "[ship name] standing by".
   */
  @Listener
  public void onServerReady(ConnectionSuccessEvent event) {
    connected = true;
  }

  /**
   * Invoked when we get a pre-game ships update. Updates the name of the ship shown in the "[ship
   * name] standing by" message on the screen.
   */
  @Listener
  public void onShipsUpdate(AllShipSettingsPacket pkt) {
    shipName = pkt.getShip(shipIndex).getName();
  }

  /**
   * The player ship has spawned.
   */
  public void onPlayerSpawn(ArtemisPlayer player) {
    alive = true;
    shipId = player.getId();
    display.onPlayerSpawn(player);
  }

  /**
   * The player ship has been updated.
   */
  public void onPlayerUpdate(ArtemisPlayer player) {
    display.onPlayerUpdate(player);
  }

  /**
   * Jump drive has been engaged.
   */
  @Listener
  public void onJump(JumpEndPacket pkt) {
    jumpEndTime = System.currentTimeMillis() + JUMP_EFFECT_DURATION_MS;
  }

  /**
   * Invoked when something hits the ship. Causes the screen to display an interface screw. Note
   * that the actual duration of the effect in the stock client is about half what the packet says,
   * so we adjust for that here.
   */
  @Listener
  public void onImpact(PlayerShipDamagePacket pkt) {
    impactEndTime = System.currentTimeMillis() + (long) (pkt.getDuration() * 500);
  }

  /**
   * Invoked when the simulation is paused/unpaused. Shows or removes the "Paused" indicator on the
   * screen.
   */
  @Listener
  public void onPause(PausePacket pkt) {
    paused = pkt.isPaused();
  }

  /**
   * The player ship has been deleted.
   */
  public void onPlayerDelete(ArtemisPlayer player) {
    alive = false;
    display.onPlayerDelete(player);
  }

  /**
   * Invoked when the simulation ends. Resets the Canvas and Display state. 
   */
  @Listener
  public void onGameOver(GameOverPacket pkt) {
    reset();
  }

  /**
   * Invoked when the connection is lost. Detaches from the ArtemisNetworkInterface and resets the
   * Canvas and Display state.
   */
  @Listener
  public void onDisconnect(DisconnectEvent event) {
    attach(null);
    connected = false;
    shipName = null;
    reset();
  }

  /**
   * Invoked when the Canvas should stop rendering. Cancels the draw Timer.
   */
  void stop() {
    if (drawTimer != null) {
      drawTimer.stop();
    }
  }

  @Override
  public void paint(Graphics g) {
    final Graphics2D g2d = (Graphics2D) g;

    // Clear the screen
    g2d.setRenderingHints(renderingHints);
    g2d.setColor(Color.BLACK);
    final Rectangle bounds = this.getBounds();
    g2d.fill(bounds);

    String msgText = null;
    Color msgColor = null;

    if (!connected) { // Show "Waiting for server" message
      msgText = localeData.string("canvas.waiting", config.getHost());
      msgColor = Color.RED;
    } else if (shipId != -1) { // Simulation is running
      if (alive) {
        if (!isScreenBlank()) {
          // Shipshake
          long impactMs = impactEndTime - System.currentTimeMillis();
          float impactStrength = Math.min((float) impactMs / IMPACT_FADEOUT_MS, 1);
          int shakeX = 0, shakeY = 0;

          if (impactStrength > 0 && config.getRenderOption(Configuration.RenderOption.IMPACT_SHAKE)) {
            float maxMagnitude = (float) Math.min(bounds.getWidth(), bounds.getHeight()) * SHAKE_MAGNITUDE;
            shakeX = computeShake(maxMagnitude, impactStrength);
            shakeY = computeShake(maxMagnitude, impactStrength);
          }

          Rectangle displayBounds = new Rectangle(bounds.x + shakeX, bounds.y + shakeY, bounds.width, bounds.height);
          g2d.setClip(displayBounds);
          display.render(g2d);
          g2d.setClip(bounds);

          // Other interface screws on impact
          if (impactStrength > 0) {
            renderImpact(g2d, impactStrength * MAX_IMPACT_STRENGTH);
          }
        }

        // Show "Paused" message if paused
        if (paused) {
          msgText = localeData.string("canvas.paused");
          msgColor = Color.YELLOW;
          g2d.setColor(SCRIM_COLOR);
          g2d.fill(bounds);
        }
      } else {
        // Player ship was destroyed; fill the screen with static (if enabled)
        renderImpact(g2d, MAX_IMPACT_STRENGTH);
      }
    } else { // Show "[ship name] standing by" message
      String shipPlaceholder = shipName != null ? shipName.toString() : localeData.string("canvas.unknown_ship");
      msgText = localeData.string("canvas.standing_by", shipPlaceholder);
      msgColor = Color.CYAN;
    }

    // If we are displaying a message, render it here.
    if (msgText != null) {
      g2d.setColor(pulseColor(msgColor, PULSE_INTERVAL, 0.5f));
      textFitter.render(g2d, bounds, msgText);

      if (shipId == -1 && instructions != null) {
        Font font = LocaleData.get().getFont((float) bounds.getWidth() / 60);
        FontMetrics fontMetrics = g2d.getFontMetrics(font);
        Rectangle2D stringBounds = fontMetrics.getStringBounds(instructions, g2d);
        float x = (float) (bounds.getWidth() - stringBounds.getWidth()) / 2;
        float y = (float) (bounds.getHeight() - stringBounds.getHeight() * 0.2);
        g2d.setFont(font);
        g2d.setColor(Color.GRAY);
        g2d.drawString(instructions, x, y);
      }
    }

    if (!drawTimer.isRunning()) {
      drawTimer.start();
    }
  }

  /**
   * Returns true if the screen should be blank in this frame (due to a jump). This is only invoked
   * while the player is alive.
   */
  private boolean isScreenBlank() {
    long diff = jumpEndTime - System.currentTimeMillis();

    if (diff < 0) {
      return false;
    }

    if (diff > JUMP_EFFECT_DURATION_MS / 2) {
      return true;
    }

    return Math.random() < diff / (float) JUMP_EFFECT_DURATION_MS;
  }

  /**
   * Resets the Canvas and Display state in response to the simulation ending.
   */
  private void reset() {
    alive = false;
    paused = false;
    shipId = -1;
    impactEndTime = 0;
    jumpEndTime = 0;
    display.reset();
  }

  /**
   * Returns a random number indicating how much the screen should be offset this frame.
   */
  private int computeShake(float maxMagnitude, float impactStrength) {
    return (int) ((Math.random() * 2 - 1) * maxMagnitude * impactStrength);
  }

  /**
   * Renders an impact effect on the screen.
   */
  private void renderImpact(Graphics2D g, float strength) {
    if (alive && config.getRenderOption(Configuration.RenderOption.IMPACT_DIM)) {
      g.setColor(new Color(0f, 0f, 0f, strength));
      final Rectangle bounds = this.getBounds();
      g.fill(bounds);
    }

    if (config.getRenderOption(Configuration.RenderOption.IMPACT_STATIC)) {
      for (int x = 0; x < getWidth(); x += STATIC_GRANULARITY){
        for (int y = 0; y < getHeight(); y += STATIC_GRANULARITY){
          int threshold = (int) (128f * strength);
          int value = (int) (Math.random() * 256);

          if (value < threshold || (255 - value) < threshold) {
            g.setColor(new Color(value, value, value));
            g.fillRect(x, y, STATIC_GRANULARITY, STATIC_GRANULARITY);
          }
        }
      }
    }
  }
}
