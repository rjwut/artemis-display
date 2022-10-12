package com.walkertribe.artemisdisplay.display.layout;

import java.util.function.Consumer;

import org.json.JSONObject;

/**
 * Abstract implementation of LayoutBuilder.
 * @author rjwut
 */
public abstract class AbstractLayoutBuilder<L extends Layout<T>, T> implements LayoutBuilder<L, T> {
  /**
   * Iterates the sub-Display entries in the given JSON layout descriptor and invokes the indicated
   * Consumer for each one.
   */
  protected void forEachDisplay(JSONObject config, Consumer<JSONObject> consumer) {
    config.getJSONArray("displays").iterator().forEachRemaining(obj -> {
      consumer.accept((JSONObject) obj);
    });
  }
}
