package com.walkertribe.artemisdisplay.display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.Canvas;
import com.walkertribe.artemisdisplay.TextFitter;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.enums.AlertStatus;
import com.walkertribe.ian.iface.Listener;
import com.walkertribe.ian.protocol.core.world.DockedPacket;
import com.walkertribe.ian.util.BoolState;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * <p>
 * Displays the ship's alert status:
 * </p>
 * <ul>
 * <li>GREEN: Normal alert status, not docked, shields down</li>
 * <li>BLUE: Normal alert status, shields down, docked (animated during docking process)</li>
 * <li>YELLOW: Normal alert status, shields up</li>
 * <li>RED: Red alert status (always animated)</li>
 * </ul>
 * @author rjwut
 */
public class AlertDisplay extends AbstractDisplay {
  private static final float WIDE_ASPECT_RATIO = 5;
  private static final float TALL_ASPECT_RATIO = 0.75f;
  private static final long ANIMATION_INTERVAL = 2000;
  private static final int BLOCK_COUNT = 10;
  private static final long OFFSET = ANIMATION_INTERVAL / BLOCK_COUNT;

  /**
   * The various alert conditions.
   */
  private enum Condition {
    GREEN(Color.GREEN),
    BLUE(Color.BLUE),
    YELLOW(Color.YELLOW),
    RED(Color.RED);

    private Color color;

    private Condition(Color color) {
      this.color = color;
    }
  }

  /**
   * The three different aspects with which the display can be shown.
   */
  private enum Aspect {
    WIDE {
      @Override
      void render(Graphics2D g, String text, Color[] colors) {
        Rectangle bounds = g.getClipBounds();
        g.setColor(colors[0]);
        TextFitter textFitter = Canvas.getTextFitter(false).ignoreDescent();
        Rectangle textArea = textFitter.render(g, bounds, text);
        int y0 = (int) bounds.getCenterY();
        int dy = (int) (0.25f * bounds.height / BLOCK_COUNT);
        int width = textArea.x - bounds.x - dy;
        int rightX = bounds.width - width;

        for (int i = 0; i < BLOCK_COUNT; i++) {
          g.setColor(colors[i]);
          int topY = y0 - (i * 2 + 1) * dy;
          int bottomY = y0 + i * 2 * dy;
          // left top
          g.fill(new Rectangle(bounds.x, topY, width, dy));
          // left bottom
          g.fill(new Rectangle(bounds.y, bottomY, width, dy));
          // right top
          g.fill(new Rectangle(rightX, topY, width, dy));
          // right bottom
          g.fill(new Rectangle(rightX, bottomY, width ,dy));
        }
      }
    },
    REGULAR {
      @Override
      void render(Graphics2D g, String text, Color[] colors) {
        Rectangle bounds = g.getClipBounds();
        g.setColor(colors[0]);
        TextFitter textFitter = Canvas.getTextFitter(false).padding(0.3f);
        Rectangle textArea = textFitter.render(g, bounds, text);
        int dx = (int) (0.5f * (textArea.x - bounds.x) / BLOCK_COUNT);
        int dy = (int) (0.5f * (textArea.y - bounds.y) / BLOCK_COUNT);

        for (int i = 0; i < BLOCK_COUNT; i++) {
          g.setColor(colors[i]);
          int n = i * 2 + 1;
          int x1 = textArea.x - (n - 1) * dx;
          int w = textArea.width + (n - 1) * dx * 2;
          // top
          g.fill(new Rectangle(x1, textArea.y - (n + 1) * dy, w, dy));
          // bottom
          g.fill(new Rectangle(x1, textArea.y + textArea.height + n * dy, w, dy));
          // left
          g.fill(new Rectangle(x1 - dx * 2, textArea.y, dx, textArea.height));
          // right
          g.fill(new Rectangle(textArea.x + textArea.width + n * dx, textArea.y, dx, textArea.height));
        }
      }
    },
    TALL {
      @Override
      void render(Graphics2D g, String text, Color[] colors) {
        Rectangle bounds = g.getClipBounds();
        g.setColor(colors[0]);
        TextFitter textFitter = Canvas.getTextFitter(false);
        Rectangle textArea = textFitter.render(g, bounds, text);
        int x0 = (int) bounds.getCenterX();
        int dx = (int) (0.25f * bounds.width / BLOCK_COUNT);
        int height = textArea.y - bounds.y - dx;
        int bottomY = bounds.height - height;

        for (int i = 0; i < BLOCK_COUNT; i++) {
          g.setColor(colors[i]);
          int leftX = x0 - (i * 2 + 1) * dx;
          int rightX = x0 + i * 2 * dx;
          // left top
          g.fill(new Rectangle(leftX, bounds.y, dx, height));
          // right top
          g.fill(new Rectangle(rightX, bounds.y, dx, height));
          // left bottom
          g.fill(new Rectangle(leftX, bottomY, dx, height));
          // right bottom
          g.fill(new Rectangle(rightX, bottomY, dx, height));
        }
      }
    };

    abstract void render(Graphics2D g, String text, Color[] colors);
  }

  private boolean docked;

  public AlertDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
  }

  /**
   * Check for undock.
   */
  @Override
  public void onPlayerUpdate(ArtemisPlayer player) {
    // If the engines are engaged, we aren't docked anymore.
    if (player.getDockingBase() == 0 || player.getWarp() > 0 || player.getImpulse() > 0) {
      docked = false;
    }
  }

  /**
   * We're docked, so stop animating the CONDITION BLUE display.
   */
  @Listener
  public void onDocked(DockedPacket pkt) {
    docked = true;
  }

  @Override
  public void renderImpl(Graphics2D g) {
    // Determine current alert condition
    Condition condition = Condition.GREEN;
    boolean animated = false;
    ArtemisPlayer player = getPlayer();

    if (player != null) {
      if (player.getAlertStatus() == AlertStatus.RED) {
        condition = Condition.RED;
        animated = true;
      } else if (BoolState.safeValue(player.getShieldsState())) {
        condition = Condition.YELLOW;
      } else if (docked) {
        condition = Condition.BLUE;
      } else if (player.getDockingBase() != 0) {
        condition = Condition.BLUE;
        animated = true;
      }
    }

    // Render
    Rectangle bounds = g.getClipBounds();
    float aspectRatio = bounds.width / (float) bounds.height;
    Aspect aspect;

    if (aspectRatio >= WIDE_ASPECT_RATIO) {
      aspect = Aspect.WIDE;
    } else if (aspectRatio <= TALL_ASPECT_RATIO) {
      aspect = Aspect.TALL;
    } else {
      aspect = Aspect.REGULAR;
    }

    String text = localeData.string("alert.condition", condition);
    Color[] colors = computeColors(condition, animated);
    aspect.render(g, text, colors);
  }

  @Override
  public void reset() {
    docked = false;
  }

  /**
   * Compute the Color array to be used to render the display.
   */
  private static Color[] computeColors(Condition condition, boolean animated) {
    long phase0 = animated ? System.currentTimeMillis() % ANIMATION_INTERVAL : 0;
    Color[] colors = new Color[BLOCK_COUNT];

    for (int i = 0; i < BLOCK_COUNT; i++) {
      long phase = (phase0 + i * OFFSET) % ANIMATION_INTERVAL;
      float alpha = 1 - (0.8f * phase / ANIMATION_INTERVAL);
      colors[i] = new Color(
          condition.color.getRed(),
          condition.color.getGreen(),
          condition.color.getBlue(),
          Math.round(alpha * 255)
      );
    }

    return colors;
  }
}
