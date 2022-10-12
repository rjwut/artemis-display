package com.walkertribe.artemisdisplay.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.walkertribe.ian.model.Model;
import com.walkertribe.ian.model.Poly;
import com.walkertribe.ian.model.Point;
import com.walkertribe.ian.protocol.core.eng.DamconTeam;
import com.walkertribe.ian.util.Grid;
import com.walkertribe.ian.util.GridCoord;
import com.walkertribe.ian.util.GridNode;
import com.walkertribe.ian.vesseldata.Vessel;

/**
 * Renders models in 3D.
 * @author rjwut
 */
public class ThreeDModelRenderer implements ModelRenderer<ThreeDRenderParams> {
  private static final int NODE_SCALE = 10;
  private static final int DAMCON_SCALE = 20;
  private static final Stroke GRID_STROKE = new BasicStroke(1);
  private static final Stroke DAMCON_STROKE = new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

  @Override
  public void render(Graphics2D g, Vessel vessel, ThreeDRenderParams params, Grid grid) {
    Model model = vessel != null ? vessel.getModel() : null;
    Map<String, double[]> pointMap = buildPointMap(model, grid, vessel, params);
    render(g, pointMap, model, params, grid);
  }

  @Override
  public void render(Graphics2D g, Model model, ThreeDRenderParams params, Grid grid) {
    Map<String, double[]> pointMap = buildPointMap(model, grid, null, params);
    render(g, pointMap, model, params, grid);
  }

  private Map<String, double[]> buildPointMap(Model model, Grid grid, Vessel vessel,
      ThreeDRenderParams params) {
    Map<String, Point> points = model.getPointMap();

    if (grid != null) {
      points.putAll(grid.toPointCloud());
    }

    return Model.transformPoints(points, params);
  }

  private void render(Graphics2D g, Map<String, double[]> pointMap, Model model,
      ThreeDRenderParams params, Grid grid) {
    List<Poly> sortedPolys = model.getPolys();

    if (params.mode.polysMustBeSorted()) {
      Collections.sort(sortedPolys, new Comparator<Poly>() {
        @Override
        public int compare(Poly o1, Poly o2) {
          double y1 = findMinY(o1, pointMap);
          double y2 = findMinY(o2, pointMap);
          return (int) Math.signum(y1 - y2);
        }
      });
    }

    for (Poly poly : sortedPolys) {
      params.renderPolygon(g, toPolygon(poly, pointMap));
    }

    // Render the system nodes
    if (grid != null) {
      // Corridors
      g.setColor(params.gridColor);
      g.setStroke(GRID_STROKE);

      for (GridNode node : grid) {
        if (!node.isAccessible()) {
          continue;
        }

        GridCoord gridCoord = node.getCoord();
        double[] coords = pointMap.get(gridCoord.toString());
        int x = gridCoord.x();
        int y = gridCoord.y();
        int z = gridCoord.z();

        if (x != 0) {
          drawCorridor(g, grid, pointMap, coords, x - 1, y, z);
        }

        if (y != 0) {
          drawCorridor(g, grid, pointMap, coords, x, y - 1, z);
        }

        if (z != 0) {
          drawCorridor(g, grid, pointMap, coords, x, y, z - 1);
        }
      }

      // Nodes
      List<GridNode> nodes = grid.getAccessibleNodes();
      nodes.sort((n1, n2) -> {
        double y1 = pointMap.get(n1.getCoord().toString())[1];
        double y2 = pointMap.get(n2.getCoord().toString())[1];
        return (int) Math.signum(y1 - y2);
      });

      for (GridNode node : nodes) {
        if (node.getSystem() == null && node.getDamage() <= 0) {
          continue;
        }

        g.setColor(node.getDamage() > 0 ? Color.RED : Color.WHITE);
        double[] coords = pointMap.get(node.getCoord().toString());
        double r = params.scale() * NODE_SCALE;
        g.fill(new Ellipse2D.Double(coords[0] - r, coords[2] - r, r * 2, r * 2));
      }

      // DAMCON teams
      g.setColor(params.damconColor);
      g.setStroke(DAMCON_STROKE);

      for (DamconTeam team : grid.getAllDamconTeams()) {
        drawDamconTeam(g, pointMap, params, team);
      }
    }
  }

  /**
   * Draws a corridor between the given origin coordinates and the (presumably adjacent) node (if it
   * exists) indicated by the given nodes coordinates.
   */
  private static void drawCorridor(Graphics2D g, Grid grid, Map<String, double[]> pointMap,
      double[] originCoords, int x, int y, int z) {
    GridCoord adjGridCoord = GridCoord.get(x, y, z);
    GridNode adjNode = grid.getNode(adjGridCoord);

    if (adjNode.isAccessible()) {
      double[] adjCoords = pointMap.get(adjGridCoord.toString());
      g.draw(new Line2D.Double(originCoords[0], originCoords[2], adjCoords[0], adjCoords[2]));
    }
  }

  /**
   * Draws a DAMCON team on the Grid.
   */
  private static void drawDamconTeam(Graphics2D g, Map<String, double[]> pointMap,
      ThreeDRenderParams params, DamconTeam team) {
    if (team == null || team.getMembers() == 0) {
      return;
    }

    double[] coords = pointMap.get("DAMCON " + team.getId());

    if (coords == null) {
      return;
    }

    int r = (int) Math.round(params.scale() * DAMCON_SCALE);
    int x = (int) Math.round(coords[0]);
    int y = (int) Math.round(coords[2]);
    int xLeft = x - r;
    int xRight = x + r;
    int yUp = y - r;
    int yDown = y + r;
    g.drawLine(x, yUp, xRight, y);
    g.drawLine(xRight, y, x, yDown);
    g.drawLine(x, yDown, xLeft, y);
    g.drawLine(xLeft, y, x, yUp);
  }

  /**
   * Finds the smallest Y value for the given Poly.
   */
  private static double findMinY(Poly poly, Map<String, double[]> pointMap) {
    double minY = Double.MAX_VALUE;
    int vertexCount = poly.pointCount();

    for (int i = 0; i < vertexCount; i++) {
      double y = pointMap.get(poly.getPointId(i))[1];
      minY = Math.min(minY, y);
    }

    return minY;
  }

  /**
   * Converts the given Poly to an AWT Polygon.
   */
  private static Polygon toPolygon(Poly poly, Map<String, double[]> vertexMap) {
    int vertexCount = poly.pointCount();
    int[] x = new int[vertexCount];
    int[] y = new int[vertexCount];

    for (int i = 0; i < vertexCount; i++) {
      String vertex = poly.getPointId(i);
      double[] p = vertexMap.get(vertex);
      x[i] = (int) Math.round(p[0]);
      y[i] = (int) Math.round(p[2]);
    }

    return new Polygon(x, y, vertexCount);
  }
}
