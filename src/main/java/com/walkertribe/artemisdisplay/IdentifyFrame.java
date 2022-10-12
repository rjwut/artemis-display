package com.walkertribe.artemisdisplay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * A JFrame which displays the monitor number in the lower-left corner of the screen, then
 * disappears after a short time.
 * @author rjwut
 */
public class IdentifyFrame extends JFrame {
  private static final long serialVersionUID = 8416848127460565437L;

  private static final int DISPLAY_TIME = 2000;

  /**
   * Creates a new IdentityFrame for each monitor.
   */
  public static final void identify() {
    SwingUtilities.invokeLater(() -> {
      int deviceCount = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;

      for (int i = 0; i < deviceCount; i++) {
        new IdentifyFrame(i);
      }
    });
  }

  /**
   * Displays the monitor number at the lower-left corner of the indicated monitor.
   */
  private IdentifyFrame(int monitorIndex) {
    super("Identifying...");
    setType(Type.UTILITY);
    setUndecorated(true);
    JLabel label = new JLabel(Integer.toString(monitorIndex + 1));
    label.setFont(Canvas.MONOSPACE_FONT.deriveFont(144f));
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setOpaque(true);
    label.setForeground(Color.WHITE);
    label.setBackground(Color.BLACK);
    add(label);
    GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    pack();
    label.setPreferredSize(new Dimension(label.getHeight(), label.getHeight()));
    pack();
    Rectangle bounds = devices[monitorIndex].getDefaultConfiguration().getBounds(); 
    setLocation(bounds.x, bounds.y + bounds.height - getHeight());
    setVisible(true);
    setAlwaysOnTop(true);
    Timer timer = new Timer(DISPLAY_TIME, ev -> {
      dispose();
    });
    timer.setRepeats(false);
    timer.start();
  }
}
