package com.walkertribe.artemisdisplay.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.walkertribe.artemisdisplay.Canvas;
import com.walkertribe.artemisdisplay.TextFitter;
import com.walkertribe.artemisdisplay.i18n.LocaleData;

/**
 * Renders a meter that displays a percentage value.
 * @author rjwut
 */
public class Meter {
  private static final float GUTTER_PERC = 0.2f;

  private LocaleData localeData;
  private String label;
  private float value;
  private float max;
  private String maxLabel;
  private ColorScheme scheme;

  /**
   * Creates a new Meter with the given label.
   */
  public Meter(String label, float value, float max, ColorScheme scheme) {
    localeData = LocaleData.get();
    this.label = label;
    this.value = value;
    this.max = max;
    this.scheme = scheme;
    maxLabel = localeData.formatPercent(1);
  }

  /**
   * Returns a Metrics object that contains all the information needed to render the Meter.
   */
  public Metrics computeMetrics(Graphics2D g, Rectangle bounds) {
    return new Metrics(g, bounds);
  }

  /**
   * Shorthand for computeMetrics(g, bounds).render(value, max).
   */
  public void render(Graphics2D g, Rectangle bounds) {
    computeMetrics(g, bounds).render();
  }

  public enum ColorScheme {
    /**
     * Gradually shifts from red to green as meter fills.
     */
    GRADIATED {
      @Override
      protected Color getColor(float value, float max) {
        return Util.computeShieldColor(value, max);
      }
    },
    /**
     * Meter is red until it's full, then it immediately turns green.
     */
    GREEN_AT_FULL {
      @Override
      protected Color getColor(float value, float max) {
        return value < max ? Color.RED : Color.GREEN;
      }
    },
    /**
     * Meter is always cyan.
     */
    SCAN {
      @Override
      protected Color getColor(float value, float max) {
        return Color.CYAN;
      }
    };

    /**
     * Returns the meter's current color.
     */
    protected abstract Color getColor(float value, float max);
  }

  /**
   * A class which computes measurements needed to render the Meter.
   */
  public class Metrics {
    private Graphics2D g;
    private Rectangle bounds;
    private Font font;
    private int labelEndX;
    private int barStartX;
    private int maxBarWidth;
    private float textY;
    private int labelX;

    /**
     * Compute metrics to render a Meter within the given bounds, using a computed font size.
     */
    private Metrics(Graphics2D g, Rectangle bounds) {
      this.g = g;
      this.bounds = bounds;
      TextFitter textFitter = Canvas.getTextFitter(false);
      TextFitter.Metrics textMetrics = textFitter.computeMetrics(g, bounds, maxLabel + " " + label);
      font = textMetrics.getLine(0).getFont();
      compute();
    }

    /**
     * Returns this Meter's font size.
     */
    public float getFontSize() {
      return font.getSize2D();
    }

    /**
     * Overrides the computed font size with the given size.
     */
    public void setFontSize(float size) {
      font = localeData.getFont(size);
      compute();
    }

    /**
     * Renders the Meter.
     */
    public void render() {
      Font prevFont = g.getFont();
      g.setFont(font);
      FontMetrics metrics = g.getFontMetrics();
      Color color;
      String dataLabel;
      int barWidth;

      if (Float.isNaN(value) || Float.isNaN(max)) {
        color = Color.WHITE;
        dataLabel = "?";
        barWidth = 0;
      } else {
        float perc = Math.max(Math.min(value / max, 1), 0);
        color = scheme.getColor(value, max);
        dataLabel = localeData.formatPercent(perc);
        barWidth = Math.round(maxBarWidth * perc);
      }

      int dataLabelX = labelEndX - metrics.stringWidth(dataLabel);
      g.setColor(Color.DARK_GRAY);
      g.fillRect(barStartX, bounds.y, maxBarWidth, bounds.height);
      g.setColor(color);
      g.drawString(dataLabel, dataLabelX, textY);
      g.fillRect(barStartX, bounds.y, barWidth, bounds.height);
      g.setColor(Color.BLACK);
      g.drawString(label, labelX, textY);
      g.setFont(prevFont);
    }

    /**
     * Computes metrics based on is Meter's current font size.
     */
    private void compute() {
      FontMetrics metrics = g.getFontMetrics(font);
      int labelWidth = metrics.stringWidth(maxLabel);
      labelEndX = bounds.x + labelWidth;
      int gutterSize = (int) (bounds.height * GUTTER_PERC);
      barStartX = labelEndX + gutterSize;
      textY = bounds.y + 0.75f * bounds.height;
      maxBarWidth = bounds.width - labelWidth - gutterSize;
      int barCenterX = barStartX + maxBarWidth / 2;
      labelX = barCenterX - metrics.stringWidth(label) / 2;
    }
  }
}
