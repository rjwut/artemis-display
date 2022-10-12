package com.walkertribe.artemisdisplay.display;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * Display showing the status of the player ship.
 * @author rjwut
 */
public class PlayerShipDisplay extends ObjectDisplay {
  public PlayerShipDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
  }

  @Override
  public void onPlayerSpawn(ArtemisPlayer player) {
    setTargetId(player.getId());
  }
}
