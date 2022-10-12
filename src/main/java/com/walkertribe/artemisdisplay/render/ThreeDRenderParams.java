package com.walkertribe.artemisdisplay.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import com.walkertribe.ian.model.RenderParams;

/**
 * RenderParams implementation for the THreeDModelRenderer.
 * @author rjwut
 */
public class ThreeDRenderParams extends RenderParams {
  RenderMode mode = RenderMode.WIREFRAME;
  Color lineColor = Color.WHITE;
  Color fillColor = Color.BLACK;
  Color gridColor = Color.WHITE;
  Color damconColor = Color.CYAN;

  /**
   * The RenderMode to use.
   */
  public ThreeDRenderParams renderMode(RenderMode mode) {
      this.mode = mode;
      return this;
  }

  /**
   * Color for wireframe lines.
   */
  public ThreeDRenderParams lineColor(Color lineColor) {
      this.lineColor = lineColor;
      return this;
  }

  /**
   * Fill color
   */
  public ThreeDRenderParams fillColor(Color fillColor) {
      this.fillColor = fillColor;
      return this;
  }

  /**
   * Grid color
   */
  public ThreeDRenderParams gridColor(Color gridColor) {
    this.gridColor = gridColor;
    return this;
  }

  /**
   * DAMCON team color
   */
  public ThreeDRenderParams damconColor(Color damconColor) {
    this.damconColor = damconColor;
    return this;
  }

  @Override
  public ThreeDRenderParams scale(double scale) {
      super.scale(scale);
      return this;
  }

  @Override
  public ThreeDRenderParams rotateX(double rotateX) {
      super.rotateX(rotateX);
      return this;
  }

  @Override
  public ThreeDRenderParams rotateY(double rotateY) {
      super.rotateY(rotateY);
      return this;
  }

  @Override
  public ThreeDRenderParams rotateZ(double rotateZ) {
      super.rotateZ(rotateZ);
      return this;
  }

  @Override
  public ThreeDRenderParams offsetX(double offsetX) {
      super.offsetX(offsetX);
      return this;
  }

  @Override
  public ThreeDRenderParams offsetY(double offsetY) {
      super.offsetY(offsetY);
      return this;
  }

  @Override
  public ThreeDRenderParams offsetZ(double offsetZ) {
      super.offsetZ(offsetZ);
      return this;
  }

  /**
   * Renders a Polygon on the given graphics context according to these
   * parameters.
   */
  void renderPolygon(Graphics2D g, Polygon polygon) {
      mode.renderPolygon(g, this, polygon);
  }
}
