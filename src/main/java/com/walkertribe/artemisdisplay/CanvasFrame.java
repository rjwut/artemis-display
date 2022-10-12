package com.walkertribe.artemisdisplay;

import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.walkertribe.ian.iface.ArtemisNetworkInterface;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * A JFrame subclass which contains only a Canvas to draw on.
 * @author rjwut
 */
public class CanvasFrame extends JFrame {
  private static final long serialVersionUID = -2554671802451303259L;

  private Canvas canvas;
  private WindowMode mode;
  private int deviceIndex = -1;

  /**
   * Displays the CanvasFrame, set to render the given Display.
   */
  public CanvasFrame(final ArtemisDisplay app) {
    super("Artemis Display");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    Configuration config = app.getConfig();
    mode = config.getWindowMode();
    deviceIndex = config.getMonitor() - 1;
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        // Go full screen if the option is set
        if (mode != WindowMode.WINDOWED) {
          CanvasFrame.this.moveDevice(0);
        }
      }

      @Override
      public void windowClosing(WindowEvent e) {
        // Stop the canvas and notify the main application class
        mode.hide(CanvasFrame.this, deviceIndex);
        canvas.stop();
        app.onWindowClose();
      }
    });
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 27) { // ESC: close the window
          if (deviceIndex != -1) {
            CanvasFrame.this.dispatchEvent(new WindowEvent(CanvasFrame.this, WindowEvent.WINDOW_CLOSING));
          }
        }

        if (deviceIndex != -1) {
          // We're running full screen, so we need keys to switch monitors
          if (e.getKeyCode() == 37 && e.isShiftDown()) { // SHIFT-LEFT
            CanvasFrame.this.moveDevice(-1);
          }

          if (e.getKeyCode() == 39 && e.isShiftDown()) { // SHIFT-RIGHT
            CanvasFrame.this.moveDevice(1);
          }
        }
      }
    });
    canvas = new Canvas(app);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(canvas, BorderLayout.CENTER);
    URL url = CanvasFrame.class.getResource("/logo.png");
    setIconImage(new ImageIcon(url).getImage());
    mode.show(this, deviceIndex);
  }

  /**
   * We've connected to the server. Delegate this event to the Canvas.
   */
  public void attach(ArtemisNetworkInterface iface) {
    canvas.attach(iface);
  }

  /**
   * Forwards player creation events to the Canvas.
   */
  public void onPlayerSpawn(ArtemisPlayer player) {
    canvas.onPlayerSpawn(player);
  }

  /**
   * Forwards the player update to the Canvas.
   */
  public void onPlayerUpdate(ArtemisPlayer player) {
    canvas.onPlayerUpdate(player);
  }

  /**
   * Forwards player deletion events to the Canvas.
   */
  public void onPlayerDelete(ArtemisPlayer player) {
    canvas.onPlayerDelete(player);
  }

  /**
   * Move the full screen display to the next (1) or previous (-1) device. This is also invoked when
   * we first go full screen with a 0 argument.
   */
  private void moveDevice(int offset) {
    GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    deviceIndex += offset;

    if (deviceIndex < 0) {
      deviceIndex = devices.length - 1;
    } else if (deviceIndex >= devices.length) {
      deviceIndex = 0;
    }

    mode.show(this, deviceIndex);
    requestFocus();
  }
}
