package com.walkertribe.artemisdisplay.display;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.render.MapProjection;
import com.walkertribe.ian.Context;

/**
 * Displays a long range sensor map.
 * @author rjwut
 */
public class LrsDisplay extends MapDisplay {
  public LrsDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx, new MapProjection(app.getWorld(), ctx, MapProjection.Mode.LRS));
  }
}
