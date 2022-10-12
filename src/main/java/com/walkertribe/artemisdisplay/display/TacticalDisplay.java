package com.walkertribe.artemisdisplay.display;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.render.MapProjection;
import com.walkertribe.ian.Context;

/**
 * Displays a tactical map.
 * @author rjwut
 */
public class TacticalDisplay extends MapDisplay {
  public TacticalDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx, new MapProjection(app.getWorld(), ctx, MapProjection.Mode.TACTICAL));
  }
}
