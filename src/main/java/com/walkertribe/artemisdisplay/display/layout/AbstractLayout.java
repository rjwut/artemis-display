package com.walkertribe.artemisdisplay.display.layout;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.display.AbstractDisplay;
import com.walkertribe.artemisdisplay.display.Display;
import com.walkertribe.artemisdisplay.render.Util;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.iface.ArtemisNetworkInterface;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * Abstract implementation of Layout. Stores entries and delegates positioning and sizing to
 * subclass.
 * @author rjwut
 */
public abstract class AbstractLayout<T> extends AbstractDisplay implements Layout<T> {
  /**
   * Keeps a Display together with its Layout parameters.
   */
  private static class Entry<T> {
    private Display display;
    private T params;

    private Entry(Display display, T params) {
      this.display = display;
      this.params = params;
    }
  }

  /**
   * Given the Layout parameters and parent Display bounds, computes the child Display's bounds.
   */
  protected abstract Rectangle computeBounds(Rectangle parentBounds, T params);

  private List<Entry<T>> entries = new LinkedList<>();
  private ArtemisNetworkInterface iface;

  protected AbstractLayout(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
  }

  @Override
  public void add(Display display, T arg) {
    entries.add(new Entry<>(display, arg));
  }

  @Override
  public void attach(ArtemisNetworkInterface iface) {
    if (this.iface == iface) {
      return;
    }

    for (Entry<T> entry : entries) {
      entry.display.attach(iface);
    }
  }

  @Override
  public void renderImpl(Graphics2D g) {
    Rectangle bounds = g.getClipBounds();

    for (Entry<T> entry : entries) {
      g.setClip(computeBounds(bounds, entry.params));
      g.setColor(Color.WHITE);
      g.setStroke(Util.ONE_PIXEL_STROKE);
      entry.display.render(g);
    }

    g.setClip(bounds);
  }

  @Override
  public void onPlayerSpawn(ArtemisPlayer player) {
    for (Entry<T> entry : entries) {
      entry.display.onPlayerSpawn(player);
    }
  }

  @Override
  public void onPlayerUpdate(ArtemisPlayer player) {
    for (Entry<T> entry : entries) {
      entry.display.onPlayerUpdate(player);
    }
  }

  @Override
  public void onPlayerDelete(ArtemisPlayer player) {
    for (Entry<T> entry : entries) {
      entry.display.onPlayerDelete(player);
    }
  }

  @Override
  public void reset() {
    for (Entry<T> entry : entries) {
      entry.display.reset();
    }
  }
}