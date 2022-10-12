package com.walkertribe.artemisdisplay.modelviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import com.walkertribe.ian.util.Grid;
import com.walkertribe.artemisdisplay.modelviewer.ModelViewer.ModelEntry;
import com.walkertribe.artemisdisplay.render.ModelRenderer;
import com.walkertribe.artemisdisplay.render.RenderMode;
import com.walkertribe.artemisdisplay.render.ThreeDModelRenderer;
import com.walkertribe.artemisdisplay.render.ThreeDRenderParams;

/**
 * A canvas on which the wireframes are drawn.
 * @author rjwut
 */
class ViewerPanel extends JPanel {
  private static final long serialVersionUID = -1829699834974656615L;

  private static final Dimension MIN_SIZE = new Dimension(400, 400);

  private ModelEntry entry;
  private ModelRenderer<ThreeDRenderParams> renderer = new ThreeDModelRenderer();
  private ThreeDRenderParams params = new ThreeDRenderParams();
  private Grid grid;

  ViewerPanel() {
    setMinimumSize(MIN_SIZE);
    setPreferredSize(MIN_SIZE);
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        repaint();
      }
    });
    params.renderMode(RenderMode.SOLID_WIREFRAME);
    params.lineColor(Color.BLACK);
    params.fillColor(Color.DARK_GRAY);
    params.gridColor(Color.LIGHT_GRAY);
  }

  /**
   * Sets what Vessel to display.
   */
  void setEntry(ModelEntry entry) {
    this.entry = entry;

    if (entry.vessel != null) {
      Grid vesselGrid = entry.vessel.getGrid();

      if (vesselGrid != null) {
        grid = new Grid(vesselGrid, true, false);
      } else {
        grid = null;
      }
    } else {
      grid = null;
    }

    repaint();
  }

  /**
   * Changes the X-axis rotation.
   */
  void setRotateX(double theta) {
    params.rotateX(theta);
    repaint();
  }

  /**
   * Changes the Y-axis rotation.
   */
  void setRotateY(double theta) {
    params.rotateY(theta);
    repaint();
  }

  /**
   * Changes the Z-axis rotation.
   */
  void setRotateZ(double theta) {
    params.rotateZ(theta);
    repaint();
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    Rectangle bounds = g.getClipBounds();
    g2.setColor(Color.BLACK);
    g2.fill(bounds);

    if (entry == null) {
      return;
    }

    double cx = bounds.getCenterX();
    double cy = bounds.getCenterY();
    params.offsetX(cx);
    params.offsetZ(cy);
    double size = Math.min(cx, cy) * 0.9;
    params.scale(entry.model.computeScale(size));

    if (entry.vessel != null) {
      renderer.render(g2, entry.vessel, params, grid);
    } else {
      renderer.render(g2, entry.model, params, null);
    }
  }
}
