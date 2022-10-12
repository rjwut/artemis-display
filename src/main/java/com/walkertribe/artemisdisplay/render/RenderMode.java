package com.walkertribe.artemisdisplay.render;

import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * Indicates how a 3D model should be rendered.
 * @author rjwut
 */
public enum RenderMode {
  /**
   * Displays the model as a transparent wireframe.
   */
  WIREFRAME {
    @Override
    public void renderPolygon(Graphics2D g, ThreeDRenderParams params, Polygon polygon) {
      g.setColor(params.lineColor);
      g.draw(polygon);
    }
  },
  /**
   * Displays the model as a solid silhouette.
   */
  SOLID {
    public void renderPolygon(Graphics2D g, ThreeDRenderParams params, Polygon polygon) {
      g.setColor(params.fillColor);
      g.fill(polygon);
    }
  },
  /**
   * Displays the model as a non-transparent wireframe.
   */
  SOLID_WIREFRAME {
    public void renderPolygon(Graphics2D g, ThreeDRenderParams params, Polygon polygon) {
      SOLID.renderPolygon(g, params, polygon);
      WIREFRAME.renderPolygon(g, params, polygon);
    }

    public boolean polysMustBeSorted() {
      return true;
    }
  };

  /**
   * Renders the given Polygon in this style.
   */
  public abstract void renderPolygon(Graphics2D g, ThreeDRenderParams params, Polygon polygon);

  /**
   * Returns true if this RenderMode requires that Polygons be rendered back to front; false
   * otherwise.
   */
  public boolean polysMustBeSorted() {
    return false;
  }
}
