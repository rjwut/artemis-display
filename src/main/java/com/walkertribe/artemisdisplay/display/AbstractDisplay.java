package com.walkertribe.artemisdisplay.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.i18n.LocaleData;
import com.walkertribe.artemisdisplay.render.Util;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.iface.ArtemisNetworkInterface;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * An abstract implementation of Display. Layouts should extend AbstractLayout rather than this
 * class.
 * @author rjwut
 */
public abstract class AbstractDisplay implements Display {
  /**
   * Performs implementation-specific rendering.
   */
  protected abstract void renderImpl(Graphics2D g);

  protected ArtemisDisplay app;
  protected Context ctx;
  protected LocaleData localeData;
  protected ArtemisNetworkInterface iface;
  private String title;
  private boolean border;

  public AbstractDisplay(ArtemisDisplay app, Context ctx) {
    this.app = app;
    this.ctx = ctx;
    this.localeData = LocaleData.get();
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public void setBorder(boolean border) {
    this.border = border;
  }

  @Override
  public void attach(ArtemisNetworkInterface iface) {
    if (iface == this.iface) {
      return;
    }

    if (iface != null) {
      iface.addListener(this);
    }

    this.iface = iface;
  }

  /**
   * Returns a reference to the player's ship, or null if we don't have one.
   */
  public ArtemisPlayer getPlayer() {
    return app.getWorld().getPlayer(app.getConfig().getShipIndex());
  }

  @Override
  public void onPlayerSpawn(ArtemisPlayer player) {
    // do nothing
  }

  @Override
  public void onPlayerUpdate(ArtemisPlayer player) {
    // do nothing
  }

  @Override
  public void onPlayerDelete(ArtemisPlayer player) {
    // do nothing
  }

  @Override
  public final void render(Graphics2D g) {
    renderImpl(g);
    Rectangle bounds = g.getClipBounds();

    if (title != null) {
      Font font = localeData.getFont((float) bounds.getWidth() / 40);
      FontMetrics fontMetrics = g.getFontMetrics(font);
      Rectangle2D stringBounds = fontMetrics.getStringBounds(title, g);
      float x = (float) (bounds.getWidth() - stringBounds.getWidth()) / 2 + bounds.x;
      float y = (float) stringBounds.getHeight() + bounds.y;
      g.setFont(font);
      g.setColor(Color.WHITE);
      g.drawString(title, x, y);
    }

    if (border) {
      g.setColor(Color.WHITE);
      g.setStroke(Util.ONE_PIXEL_STROKE);
      g.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
    }
  }
}
