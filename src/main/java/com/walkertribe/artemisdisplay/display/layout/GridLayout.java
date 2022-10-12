package com.walkertribe.artemisdisplay.display.layout;

import java.awt.Rectangle;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.ian.Context;

/**
 * Layout implementation that divides the area into a nodes of equally-sized rectangles.
 * @author rjwut
 */
public class GridLayout extends AbstractLayout<GridLayout.Params> {
  /**
   * The coordinates and span counts of a nodes cell. The upper-left cell is (0, 0).
   */
  public static class Params {
    private int r;
    private int c;
    private int rowSpan;
    private int colSpan;

    public Params(int r, int c, int rowSpan, int colSpan) {
     this.r = r;
     this.c = c;
     this.rowSpan = rowSpan;
     this.colSpan = colSpan;
    }
  }

  private int rowCount;
  private int colCount;

  /**
   * Creates a new GridLayout with the indicated number of rows and columns.
   */
  public GridLayout(ArtemisDisplay app, Context ctx, int rowCount, int colCount) {
    super(app, ctx);

    if (rowCount < 1) {
      throw new IllegalArgumentException("Invalid row count: " + rowCount);
    }

    if (colCount < 1) {
      throw new IllegalArgumentException("Invalid column count: " + colCount);
    }

    this.rowCount = rowCount;
    this.colCount = colCount;
  }

  @Override
  protected Rectangle computeBounds(Rectangle parentBounds, Params params) {
    float w = (float) parentBounds.getWidth() / colCount;
    float h = (float) parentBounds.getHeight() / rowCount;
    int x = Math.round(params.c * w + parentBounds.x);
    int y = Math.round(params.r * h + parentBounds.y);
    return new Rectangle(x, y, Math.round(w * params.colSpan), Math.round(h * params.rowSpan));
  }
}
