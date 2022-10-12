package com.walkertribe.artemisdisplay.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.walkertribe.artemisdisplay.i18n.LocaleData;

/**
 * A class that is capable of rendering tabular data. You can create a single Table object and
 * define its columns, then call body() each time you are about to render to supply the table with
 * data. Each row is represented with an object of type T.
 * @author rjwut
 */
public class Table<T> {
  private List<Column> columns = new LinkedList<>();
  private int tableWidthChars;

  /**
   * Adds a column to the Table.
   */
  public Table<T> column(String label, CellRenderer<T> renderer) {
    return column(label, renderer, label.length());
  }

  /**
   * Adds a column to the Table.
   */
  public Table<T> column(String label, CellRenderer<T> renderer, int charWidth) {
    columns.add(new Column(label, renderer, charWidth));
    tableWidthChars += charWidth + (columns.size() > 1 ? 1 : 0);
    return this;
  }

  /**
   * Creates a table Body in preparation for a single render.
   */
  public Body body() {
    return new Body();
  }

  /**
   * The Body of the Table. This is created new for each render.
   */
  public class Body {
    private LocaleData localeData;
    private List<T> rows = new ArrayList<>();

    private Body() {
      localeData = LocaleData.get();
    }

    /**
     * Adds a row to the Body.
     */
    public Body row(T obj) {
      rows.add(obj);
      return this;
    }

    /**
     * Adds a row for each object in the given Collection.
     */
    public Body rows(Collection<T> objs) {
      rows.addAll(objs);
      return this;
    }

    /**
     * Renders the Table on the given Graphics2D context within the current clip bounds.
     */
    public void render(Graphics2D g) {
      Rectangle bounds = g.getClipBounds();
      Font font = localeData.getFont();
      FontMetrics fontMetrics = g.getFontMetrics(font);
      int charWidthPixels = fontMetrics.stringWidth("X");
      int tableWidthPixels = tableWidthChars * charWidthPixels;
      double scale = 0.9 * bounds.getWidth() / tableWidthPixels;
      float fontSize = font.getSize() * (float) scale;
      font = font.deriveFont(fontSize);
      fontMetrics = g.getFontMetrics(font);
      charWidthPixels = fontMetrics.stringWidth("X");
      tableWidthPixels = tableWidthChars * charWidthPixels;
      int tableHeightPixels = fontMetrics.getHeight() * (rows.size() + 1);
      int maxHeight = (int) Math.round(bounds.getHeight() * 0.9);

      if (tableHeightPixels > maxHeight) {
        scale = maxHeight / (double) tableHeightPixels;
        fontSize *= (float) scale;
        font = font.deriveFont(fontSize);
        fontMetrics = g.getFontMetrics(font);
        charWidthPixels = fontMetrics.stringWidth("X");
        tableWidthPixels = tableWidthChars * charWidthPixels;
        tableHeightPixels = fontMetrics.getHeight() * (rows.size() + 1);
      }

      int dy = fontMetrics.getHeight();
      int y = (int) Math.round(bounds.getCenterY() - tableHeightPixels / 2 + fontMetrics.getAscent());
      int x0 = (int) Math.round(bounds.getCenterX() - tableWidthPixels / 2);
      int x = x0;
      g.setFont(font);
      g.setColor(Color.GRAY);

      for (Column column : columns) {
        g.drawString(column.label, x, y);
        x += (column.width + 1) * charWidthPixels;
      }

      for (T row : rows) {
        y += dy;
        x = x0;

        for (Column column : columns) {
          g.setColor(column.renderer.getColor(row));
          String txt = column.renderer.toString(localeData, row);
          int xTxt = x + column.width * charWidthPixels - fontMetrics.stringWidth(txt);
          g.drawString(txt, xTxt, y);
          x += (column.width + 1) * charWidthPixels;
        }
      }
    }
  }

  /**
   * Interface for objects which can render a cell in the Table.
   */
  public static interface CellRenderer<T> {
    /**
     * Returns the Color that should be used to render the cell.
     */
    Color getColor(T value);

    /**
     * Returns the contents of the cell.
     */
    String toString(LocaleData localeData, T value);
  }

  /**
   * Represents a single column in the Table.
   */
  private class Column {
    private String label;
    private CellRenderer<T> renderer;
    private int width;

    /**
     * Creates a new Column with the given label and whose cells are rendered with the given
     * CellRenderer. The width argument states how many characters wide the Column will be.
     */
    private Column(String label, CellRenderer<T> renderer, int width) {
      this.label = label;
      this.renderer = renderer;
      this.width = width;
    }
  }
}
