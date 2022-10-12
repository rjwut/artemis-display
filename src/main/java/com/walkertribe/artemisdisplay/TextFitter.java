package com.walkertribe.artemisdisplay;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Class that will fit text within the given bounds. Each line's font size is adjusted to fill the
 * whole width. If this causes the text to be too tall, all lines are scaled down to fit the height.
 * @author rjwut
 */
public class TextFitter {
  private Font font;
  private float lineHeight = 1f;
  private boolean ignoreDescent;
  private float padding;

  /**
   * An object which contains the computed measurements for rendering the fitted text and can
   * perform the actual render.
   */
  public class Metrics extends Rectangle {
    private static final long serialVersionUID = -2395842168265221632L;

    private Graphics2D g;
    private Line[] lines;

    /**
     * Computes metrics for the indicated render operation.
     */
    private Metrics(Graphics2D g, Rectangle bounds, String text) {
      this.g = g;
      bounds = addPadding(bounds);
      String[] lineStrings = text.split("\\n");
      lines = new Line[lineStrings.length];

      for (int i = 0; i < lineStrings.length; i++) {
        lines[i] = new Line(lineStrings[i]);
      }

      scaleToWidth(g, bounds.width);

      if (height > bounds.height) {
        scaleToHeight(g, bounds.height);
      }

      x = (bounds.width - width) / 2 + bounds.x;
      y = (bounds.height - height) / 2 + bounds.y;
    }

    /**
     * Returns the number of lines in this render.
     */
    public int getLineCount() {
      return lines.length;
    }

    /**
     * Returns the indicated Line in the render.
     */
    public Line getLine(int i) {
      return lines[i];
    }

    /**
     * Performs the rendering.
     */
    public void render() {
      int curY = y;

      for (Line line : lines) {
        line.render(g, x, curY + line.ascent);
        curY += line.height;
      }
    }

    /**
     * Resets the scale of each Line to the given width.
     */
    private void scaleToWidth(Graphics2D g, int width) {
      this.width = width;
      height = 0;

      for (Line line : lines) {
        line.scaleToWidth(g, width);
        height += line.height;
      }
    }

    /**
     * Adjusts the existing scale of each Line so that the total height is the given height.
     */
    private void scaleToHeight(Graphics2D g, int height) {
      float scaleAdjust = height / (float) this.height;
      width = Math.round(width * scaleAdjust);
      this.height = height;

      for (Line line : lines) {
        line.scaleBy(g, scaleAdjust);
      }
    }
  }

  /**
   * Creates a new TextFitter that renders text in the given Font.
   */
  public TextFitter(Font font) {
    this.font = font;
  }

  /**
   * Sets the line height. Default is 1.0, smaller values make lines closer together, larger values
   * do the opposite.
   */
  public TextFitter lineHeight(float lineHeight) {
    this.lineHeight = lineHeight;
    return this;
  }

  /**
   * If set, no height will be allocated for descenders. Typically appropriate for ALL CAPS text.
   */
  public TextFitter ignoreDescent() {
    ignoreDescent = true;
    return this;
  }

  /**
   * Sets the padding amount. This value will be multiplied to the extent of each dimension to
   * compute the size of padding in that dimension. Default is 0.0 (no padding).
   */
  public TextFitter padding(float padding) {
    this.padding = padding / 2;
    return this;
  }

  /**
   * Performs the computations needed to render the given text within the indicated bounds, then
   * returns a Metrics object containing that data. You can perform the actual rendering by invoking
   * Metrics.render().
   */
  public Metrics computeMetrics(Graphics2D g, Rectangle bounds, String text) {
    return new Metrics(g, bounds, text);
  }

  /**
   * Shorthand for computeMetrics(g, bounds, text).render(). This is useful if you don't need to
   * examine the Metrics before the render happens.
   */
  public Metrics render(Graphics2D g, Rectangle bounds, String text) {
    Metrics metrics = computeMetrics(g, bounds, text);
    metrics.render();
    return metrics;
  }

  /**
   * Returns a Rectangle that reflects an adjustment of the given Rectangle to add padding.
   */
  private Rectangle addPadding(Rectangle bounds) {
    int xPadding = Math.round(bounds.width * padding);
    int yPadding = Math.round(bounds.height * padding);
    return new Rectangle(
        bounds.x + xPadding,
        bounds.y + yPadding,
        bounds.width - xPadding * 2,
        bounds.height - yPadding * 2
    );
  }

  /**
   * Represents a single Line of text.
   */
  public class Line {
    private String text;
    private Font font;
    private float scale;
    private int ascent;
    private int height;

    /**
     * Creates a new Line which displays the given text.
     */
    private Line(String text) {
      this.text = text;
    }

    /**
     * Returns the text to be rendered on this Line.
     */
    public String getText() {
      return text;
    }

    /**
     * Returns the computed Font for this Line.
     */
    public Font getFont() {
      return font;
    }

    /**
     * Sets the scale of this Line so that it has the given width.
     */
    private void scaleToWidth(Graphics2D g, int width) {
      int lineWidth = getMetrics(g, TextFitter.this.font).stringWidth(text);
      scale(g, (float) width / lineWidth);
    }

    /**
     * Multiplies the current scale of this Line by the given amount.
     */
    private void scaleBy(Graphics2D g, float scaleAdjust) {
      scale(g, scale * scaleAdjust);
    }

    /**
     * Renders this Line and the indicated position.
     */
    private void render(Graphics2D g, float x, float y) {
      g.setFont(font);
      g.drawString(text, x, y);
    }

    /**
     * Sets this Line's scale and recomputes metrics.
     */
    private void scale(Graphics2D g, float scale) {
      this.scale = scale;
      font = TextFitter.this.font.deriveFont(TextFitter.this.font.getSize2D() * scale);
      FontMetrics metrics = getMetrics(g, font);
      ascent = Math.round(metrics.getAscent() * lineHeight);
      height = Math.round((ignoreDescent ? metrics.getAscent() : metrics.getHeight()) * lineHeight);
    }

    /**
     * Returns a FontMetrics object for the given Font.
     */
    private FontMetrics getMetrics(Graphics2D g, Font fontToGetMetricsFor) {
      return g.getFontMetrics(fontToGetMetricsFor);
    }
  }
}
