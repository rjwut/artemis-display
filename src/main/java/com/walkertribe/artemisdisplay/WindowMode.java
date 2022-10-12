package com.walkertribe.artemisdisplay;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JFrame;

/**
 * The available window modes to use to show the display.
 * @author rjwut
 */
public enum WindowMode {
  /**
   * If available, the window displays in exclusive fullscreen mode. If exclusive fullscreen is not
   * available, it will display as WINDOWED_FULLSCREEN.
   */
  FULLSCREEN("Fullscreen") {
    @Override
    public void show(JFrame frame, int monitorIndex) {
      if (!frame.isVisible()) {
        frame.setUndecorated(true);
        frame.pack();
        frame.setVisible(true);
      }

      GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
      devices[monitorIndex].setFullScreenWindow(frame);
    }

    @Override
    public void hide(JFrame frame, int monitorIndex) {
      GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
      devices[monitorIndex].setFullScreenWindow(null);
      frame.dispose();
    }
  },
  /**
   * Visually similar to FULLSCREEN, but uses a maximized, always-on-top window instead of exclusive
   * fullscreen mode.
   */
  WINDOWED_FULLSCREEN("Windowed fullscreen") {
    @Override
    public void show(JFrame frame, int monitorIndex) {
      if (!frame.isVisible()) {
        frame.setUndecorated(true);
      }

      WINDOWED.show(frame, monitorIndex);
      frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
      frame.setAlwaysOnTop(true);
    }

    @Override
    public void hide(JFrame frame, int monitorIndex) {
      frame.dispose();
    }
  },
  /**
   * The window displays as a regular, resizable window with a title bar.
   */
  WINDOWED("Windowed") {
    @Override
    public void show(JFrame frame, int monitorIndex) {
      Rectangle bounds = getMonitorBounds(monitorIndex);

      if (!frame.isVisible()) {
        int width = (int) (bounds.getWidth() / 2);
        int height = (int) (bounds.getHeight() / 2);
        Dimension minSize = new Dimension(width, height);
        Container container = frame.getContentPane();
        container.setMinimumSize(minSize);
        container.setPreferredSize(minSize);
        frame.pack();
        frame.setVisible(true);
      }

      int x = bounds.x + (bounds.width - frame.getWidth()) / 2;
      int y = bounds.y + (bounds.height - frame.getHeight()) / 2;
      frame.setLocation(x, y);
    }

    @Override
    public void hide(JFrame frame, int monitorIndex) {
      frame.dispose();
    }
  };

  /**
   * Returns the bounds of the given monitor.
   */
  private static Rectangle getMonitorBounds(int monitorIndex) {
    GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    return devices[monitorIndex].getDefaultConfiguration().getBounds();
  }

  /**
   * Show the given JFrame in this WindowMode on the indicated monitor.
   */
  public abstract void show(JFrame frame, int monitorIndex);

  /**
   * Remove the previously show()n JFrame.
   */
  public abstract void hide(JFrame frame, int monitorIndex);

  private String label;

  private WindowMode(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }
}
