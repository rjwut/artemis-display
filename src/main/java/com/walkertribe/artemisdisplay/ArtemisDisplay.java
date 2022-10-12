package com.walkertribe.artemisdisplay;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

import com.walkertribe.artemisdisplay.i18n.LocaleData;
import com.walkertribe.ian.enums.Console;
import com.walkertribe.ian.iface.ArtemisNetworkInterface;
import com.walkertribe.ian.iface.DisconnectEvent;
import com.walkertribe.ian.iface.Listener;
import com.walkertribe.ian.protocol.core.EndGamePacket;
import com.walkertribe.ian.protocol.core.eng.EngRequestGridUpdatePacket;
import com.walkertribe.ian.protocol.core.setup.AllShipSettingsPacket;
import com.walkertribe.ian.protocol.core.setup.ReadyPacket;
import com.walkertribe.ian.protocol.core.setup.SetConsolePacket;
import com.walkertribe.ian.protocol.core.setup.SetShipPacket;
import com.walkertribe.ian.util.Grid;
import com.walkertribe.ian.world.ArtemisObject;
import com.walkertribe.ian.world.ArtemisPlayer;
import com.walkertribe.ian.world.World;
import com.walkertribe.ian.world.WorldListener;

/**
 * The main class to launch the ArtemisDisplay application.
 * @author rjwut
 */
public class ArtemisDisplay implements Connector.Listener, WorldListener {
  public static void main(String[] args) {
    try {
      setLookAndFeel();
      Configuration config = new Configuration(args);

      if (config.getAction() == Configuration.Action.HELP) {
        Configuration.printUsage();
        return;
      }

      if (config.getAction() == Configuration.Action.EXPORT_STRINGS) {
        LocaleData.exportStrings();
        return;
      }

      LocaleData.getSupportedLocales(); // initialize i18n

      if (!config.isForceDialog() && config.isReady()) {
        new ArtemisDisplay(config);
      } else {
        SwingUtilities.invokeLater(() -> {
          new ConfigDialog(config, finalConfig -> {
            new ArtemisDisplay(finalConfig);
          });
        });
      }
    } catch (Exception ex) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.append(ex.getMessage())
        .append("\n\nError details:\n")
        .append(ex.getClass().getCanonicalName());
      StackTraceElement[] trace = ex.getStackTrace();

      for (int i = 1; i < trace.length; i++) {
        pw.append('\n').append(trace[i].toString());
      }

      JOptionPane.showMessageDialog(
        null,
        sw.toString(),
        "Artemis Display",
        JOptionPane.ERROR_MESSAGE
      );
    }
  }

  /**
   * Sets the UI look and feel to Nimbus if available.
   */
  private static void setLookAndFeel() {
    try {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        String className = info.getClassName();

        if (className.endsWith("NimbusLookAndFeel")) {
          UIManager.setLookAndFeel(className);
        }
      }
    } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
      // do nothing
    }
  }

  private Configuration config;
  private CanvasFrame frame;
  private Thread connectorThread;
  private ArtemisNetworkInterface iface;
  private World world = new World();
  private Grid grid;
  private boolean ready;

  /**
   * Initializes the configuration, system grid, and World, then launches the window and starts
   * listening for the server.
   */
  public ArtemisDisplay(final Configuration config) {
    this.config = config;
    grid = new Grid(config.getShipIndex(), config.getContext());
    world.addListener(this);

    // Show the display
    SwingUtilities.invokeLater(() -> {
      frame = new CanvasFrame(ArtemisDisplay.this);
      frame.setVisible(true);
      watchForServer();
    });
  }

  @Override
  public void attach(ArtemisNetworkInterface iface) {
    if (this.iface == iface) {
      return;
    }

    if (iface != null) {
      ready = false;
      iface.addListener(world);
      iface.addListener(grid);
      iface.addListener(this);

      if (frame != null) {
        frame.attach(iface);
      }

      if (!iface.isConnected()) {
        iface.start();
      }

      iface.send(new EngRequestGridUpdatePacket());
    }

    this.iface = iface;
  }

  @Override
  public void onException(Exception ex) {
    ex.printStackTrace();
    shutdown();
  }

  /**
   * We've received the list of ships. Select our ship, claim a data console, and signal readiness. 
   */
  @Listener
  public void onAllShipSettings(AllShipSettingsPacket pkt) {
    if (ready) {
      return;
    }

    ready = true;

    int shipIndex = config.getShipIndex();

    if (shipIndex != 0) {
      iface.send(new SetShipPacket(shipIndex));
    }

    iface.send(new SetConsolePacket(Console.COMMUNICATIONS, true));
    iface.send(new SetConsolePacket(Console.DATA, true));
    iface.send(new ReadyPacket());
  }

  @Override
  public void onCreate(ArtemisObject obj) {
    // don't care
  }

  /**
   * A player ship has spawned; check to see if it's this player.
   */
  @Override
  public void onPlayerSpawn(ArtemisPlayer player) {
    if (player.getShipIndex() == config.getShipIndex()) {
      frame.onPlayerSpawn(player);
    }
  }

  /**
   * There's a player ship update; check to see if it's this player.
   */
  @Listener
  public void onPlayerUpdate(ArtemisPlayer update) {
    ArtemisPlayer player = world.getPlayer(config.getShipIndex());

    if (player != null && player.getId() == update.getId()) {
      frame.onPlayerUpdate(player);
    }
  }

  /**
   * An object has been deleted; see if it's this player's ship. If so, the ship has been destroyed
   * or the simulation is over.
   */
  @Override
  public void onDelete(ArtemisObject obj) {
    if (obj instanceof ArtemisPlayer) {
      ArtemisPlayer player = (ArtemisPlayer) obj;

      if (player.getShipIndex() == config.getShipIndex()) {
        frame.onPlayerDelete(player);
      }
    }
  }

  /**
   * Invoked when the simulation ends. Resets the Canvas and Display state.
   */
  @Listener
  public void onGameOver(EndGamePacket pkt) {
    world.clear();
    grid.clear();
  }

  /**
   * Invoked when the connection is lost. Detaches from the ArtemisNetworkInterface and resets the
   * Canvas and Display state.
   */
  @Listener
  public void onDisconnect(DisconnectEvent event) {
    attach(null);
    watchForServer();
  }

  /**
   * The display window has closed; shut down the application.
   */
  public void onWindowClose() {
    shutdown();
  }

  /**
   * Continually attempt to connect to the server until it appears.
   */
  private void watchForServer() {
    connectorThread = new Thread(new Connector(config.getHost(), this));
    connectorThread.setDaemon(true);
    connectorThread.start();
  }

  /**
   * Make sure that all asynchronous processes are stopped.
   */
  private void shutdown() {
    if (connectorThread != null && connectorThread.isAlive()) {
      connectorThread.interrupt();
    }

    if (iface != null && iface.isConnected()) {
      iface.stop();
    }

    if (frame != null && frame.isActive()) {
      frame.dispose();
    }
  }

  /**
   * Returns the Configuration object.
   */
  public Configuration getConfig() {
    return config;
  }

  /**
   * Returns the World object.
   */
  public World getWorld() {
    return world;
  }

  /**
   * Returns the Grid object.
   */
  public Grid getGrid() {
    return grid;
  }
}
