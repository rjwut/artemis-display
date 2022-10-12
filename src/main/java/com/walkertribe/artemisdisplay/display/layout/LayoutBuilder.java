package com.walkertribe.artemisdisplay.display.layout;

import org.json.JSONObject;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.ian.Context;

/**
 * Interface for objects which can convert a JSONObject to a Layout.
 * @author rjwut
 */
public interface LayoutBuilder<L extends Layout<T>, T> {
  /**
   * Returns a Layout constructed according to the spec given in the JSONObject.
   */
  public Layout<T> build(ArtemisDisplay app, Context ctx, LayoutParser parser, JSONObject layoutConfig);
}
