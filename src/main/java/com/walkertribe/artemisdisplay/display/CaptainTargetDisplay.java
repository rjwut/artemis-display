package com.walkertribe.artemisdisplay.display;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * Display showing the status of the captain's current target.
 * @author rjwut
 */
public class CaptainTargetDisplay extends ObjectDisplay {
  public CaptainTargetDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
  }

  @Override
  public void onPlayerUpdate(ArtemisPlayer player) {
    super.onPlayerUpdate(player);
    int id = player.getCaptainTarget();

    if (id != -1) {
      setTargetId(id);
    }
  }
}
