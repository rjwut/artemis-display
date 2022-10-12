package com.walkertribe.artemisdisplay.display;

import java.awt.Graphics2D;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.render.MapProjection;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.enums.GameType;
import com.walkertribe.ian.iface.Listener;
import com.walkertribe.ian.protocol.core.GameStartPacket;

/**
 * Abstract Display implementation that renders a map.
 * @author rjwut
 */
public abstract class MapDisplay extends AbstractDisplay {
  private MapProjection projection;
  private GameType gameType;

  public MapDisplay(ArtemisDisplay app, Context ctx, MapProjection projection) {
    super(app, ctx);
    this.projection = projection;
  }

  @Listener
  public void onGameStart(GameStartPacket pkt) {
    gameType = pkt.getGameType();
  }

  @Override
  public void reset() {
    gameType = null;
  }

  @Override
  protected void renderImpl(Graphics2D g) {
    projection.render(g, getPlayer(), gameType);
  }
}
