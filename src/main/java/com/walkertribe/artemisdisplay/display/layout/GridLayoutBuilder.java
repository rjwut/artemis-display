package com.walkertribe.artemisdisplay.display.layout;

import org.json.JSONObject;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.ian.Context;

/**
 * LayoutBuilder implementation for GridLayout.
 * @author rjwut
 */
public class GridLayoutBuilder extends AbstractLayoutBuilder<GridLayout, GridLayout.Params> {
  @Override
  public Layout<GridLayout.Params> build(ArtemisDisplay app, Context ctx, LayoutParser parser,
      JSONObject layoutConfig) {
    final int rows = layoutConfig.getInt("rows");

    if (rows < 1) {
      throw new IllegalArgumentException("You must have at least one row: " + rows);
    }

    final int cols = layoutConfig.getInt("cols");

    if (cols < 1) {
      throw new IllegalArgumentException("You must have at least one column: " + cols);
    }

    GridLayout layout = new GridLayout(app, ctx, rows, cols);
    forEachDisplay(layoutConfig, displayConfig -> {
      int row = displayConfig.getInt("row");

      if (row < 0 || row >= rows) {
        throw new IllegalArgumentException("Illegal row index: " + row);
      }

      int col = displayConfig.getInt("col");

      if (col < 0 || col >= cols) {
        throw new IllegalArgumentException("Illegal column index: " + col);
      }

      int rowSpan = displayConfig.has("rowSpan") ? displayConfig.getInt("rowSpan") : 1;

      if (rowSpan < 1) {
        throw new IllegalArgumentException("Illegal row span: " + rowSpan);
      }

      if (row + rowSpan > rows) {
        throw new IllegalArgumentException("Row span exceeds number of rows: Row " + row + ", span " + rowSpan);
      }

      int colSpan = displayConfig.has("colSpan") ? displayConfig.getInt("colSpan") : 1;

      if (colSpan < 1) {
        throw new IllegalArgumentException("Illegal col span: " + colSpan);
      }

      if (col + colSpan > cols) {
        throw new IllegalArgumentException("Col span exceeds number of columns: column " + col + ", span " + colSpan);
      }

      GridLayout.Params params = new GridLayout.Params(row, col, rowSpan, colSpan);
      layout.add(parser.build(displayConfig), params);
    });
    return layout;
  }
}
