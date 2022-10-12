package com.walkertribe.artemisdisplay.render;

import java.awt.Graphics2D;

import com.walkertribe.ian.model.Model;
import com.walkertribe.ian.util.Grid;
import com.walkertribe.ian.vesseldata.Vessel;

/**
 * Interface for classes which are capable of rendering Models to a Graphics2D context. The generic
 * <T> should be the class of the parameters object which is passed into the render() methods.
 * 
 * Implementations may wish to take advantage of IAN's Matrix class for performing matrix
 * transformations.
 * 
 * @author rjwut
 */
public interface ModelRenderer<T> {
  /**
   * Renders a given model.
   */
  public void render(Graphics2D g, Model model, T params, Grid grid);

  /**
   * Renders a given Vessel. Implementations may support rendering of system nodes and beam arcs (if
   * the given Vessel has them). If a SystemGrid is provided, that will be rendered on top.
   */
  public default void render(Graphics2D g, Vessel vessel, T params, Grid grid) {
    render(g, vessel != null ? vessel.getModel() : null, params, grid);
  }
}
