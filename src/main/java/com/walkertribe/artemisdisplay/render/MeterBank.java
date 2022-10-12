package com.walkertribe.artemisdisplay.render;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles rendering of a group of Meters so that they all have the same font size.
 * @author rjwut
 */
public class MeterBank {
  private Graphics2D g;
  private List<Meter.Metrics> meters = new LinkedList<>();

  public MeterBank(Graphics2D g) {
    this.g = g;
  }

  /**
   * Adds a meter to this bank.
   */
  public void addMeter(Rectangle bounds, String label, float value, float max, Meter.ColorScheme scheme) {
    meters.add(new Meter(label, value, max, scheme).computeMetrics(g, bounds));
  }

  /**
   * Renders the meter bank.
   */
  public void render() {
    float fontSize = Float.MAX_VALUE;

    for (Meter.Metrics meter : meters) {
      fontSize = Math.min(fontSize, meter.getFontSize());
    }

    for (Meter.Metrics meter : meters) {
      meter.setFontSize(fontSize);
      meter.render();
    }
  }
}
