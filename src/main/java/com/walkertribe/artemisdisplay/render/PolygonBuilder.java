package com.walkertribe.artemisdisplay.render;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplifies drawing polygonal shapes where the vertices are declared using polar coordinates.
 * @author rjwut
 */
public class PolygonBuilder {
  private Path2D.Double path = new Path2D.Double();
  private List<Point2D.Double> points = new ArrayList<>();
  private double offsetX;
  private double offsetY;

  /**
   * Creates a new PolygonBuilder, using the given coordinates as the origin.
   */
  public PolygonBuilder(double offsetX, double offsetY) {
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }

  /**
   * Adds a point defined by the given polar coordinates as the next vertex for the polygon.
   */
  public PolygonBuilder add(double theta, double r) {
    double x = Math.cos(theta) * r + offsetX;
    double y = -Math.sin(theta) * r + offsetY;
    points.add(new Point2D.Double(x, y));

    if (path.getCurrentPoint() == null) {
      path.moveTo(x, y);
    } else {
      path.lineTo(x, y);
    }

    return this;
  }

  /**
   * Returns a Point2D.Double object representing the nth vertex added to the polygon.
   */
  public Point2D.Double get(int n) {
    return points.get(n);
  }

  /**
   * Returns a Point2D.Double that represents the average position of the vertices of the polygon.
   */
  public Point2D.Double average() {
    double x = 0;
    double y = 0;

    for (Point2D.Double point : points) {
      x += point.x;
      y += point.y;
    }

    int pointCount = points.size();
    return new Point2D.Double(x / pointCount, y / pointCount);
  }

  /**
   * Returns a Path2D.Double of the polygon.
   */
  public Path2D.Double toPath() {
    path.closePath();
    return path;
  }
}
