package com.walkertribe.artemisdisplay.display.layout;

import org.json.JSONObject;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.display.Display;
import com.walkertribe.ian.Context;

/**
 * A Display subtype that acts as a parent to multiple child Displays (which may be Layouts
 * themselves). Implementations provide different ways of visually organizing child Displays.
 * @author rjwut
 */
public interface Layout<T> extends Display {
  /**
   * The available types of Layouts.
   */
  public static enum Type {
    ABSOLUTE {
      @Override
      public LayoutBuilder<?, ?> builder() {
        return new AbsoluteLayoutBuilder();
      }
    },
    GRID {
      @Override
      public LayoutBuilder<?, ?> builder() {
        return new GridLayoutBuilder();
      }
    };

    public abstract LayoutBuilder<?, ?> builder();

    public Layout<?> newInstance(ArtemisDisplay app, Context ctx, LayoutParser parser,
        JSONObject layoutConfig) {
      return builder().build(app, ctx, parser, layoutConfig);
    }
  }

  /**
   * Adds a new Display to this Layout.
   */
  void add(Display display, T arg);
}
