package com.walkertribe.artemisdisplay.display.layout;

import org.json.JSONObject;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.Util;
import com.walkertribe.ian.Context;

/**
 * LayoutBuilder implementation for AbsoluteLayouts.
 * @author rjwut
 */
public class AbsoluteLayoutBuilder extends AbstractLayoutBuilder<AbsoluteLayout, AbsoluteLayout.Params> {
  @Override
  public Layout<AbsoluteLayout.Params> build(ArtemisDisplay app, Context ctx, LayoutParser parser,
      JSONObject layoutConfig) {
    AbsoluteLayout layout = new AbsoluteLayout(app, ctx);
    forEachDisplay(layoutConfig, displayConfig -> {
      float x = Util.parseFloat(displayConfig.getString("x"));
      float y = Util.parseFloat(displayConfig.getString("y"));
      float width = Util.parseFloat(displayConfig.getString("width"));
      float height = Util.parseFloat(displayConfig.getString("height"));
      AbsoluteLayout.Params params = new AbsoluteLayout.Params(x, y, width, height);
      layout.add(parser.build(displayConfig), params);
    });
    return layout;
  }
}
