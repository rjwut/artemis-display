package com.walkertribe.artemisdisplay.display.layout;

import java.awt.Rectangle;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.ian.Context;

/**
 * Layout implementation where each display is positioned and sized specifically.
 * @author rjwut
 */
public class AbsoluteLayout extends AbstractLayout<AbsoluteLayout.Params> {
  /**
   * The position and size for this display. These are expressed as percentages of the parent
   * display area. 
   */
  public static class Params {
    private float xPerc;
    private float yPerc;
    private float wPerc;
    private float hPerc;

    public Params(float xPerc, float yPerc, float wPerc, float hPerc) {
      this.xPerc = xPerc;
      this.yPerc = yPerc;
      this.wPerc = wPerc;
      this.hPerc = hPerc;
    }

    /**
     * Returns the bounds of the display whose position and size are determined by this object.
     */
    private Rectangle getBounds(Rectangle bounds) {
      float w = (float) bounds.getWidth();
      float h = (float) bounds.getHeight();
      int x = (int) Math.round(w * xPerc + bounds.x);
      int y = (int) Math.round(h * yPerc + bounds.y);
      w = Math.round(w * wPerc);
      h = Math.round(h * hPerc);
      return new Rectangle(x, y, (int) w, (int) h);
    }
  }

  public AbsoluteLayout(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
  }

  @Override
  protected Rectangle computeBounds(Rectangle parentBounds, Params params) {
    return params.getBounds(parentBounds);
  }
}
