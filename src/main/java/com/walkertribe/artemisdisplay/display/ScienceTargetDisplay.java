package com.walkertribe.artemisdisplay.display;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * Display showing the status of the science officer's current target.
 * @author rjwut
 */
public class ScienceTargetDisplay extends ObjectDisplay {
  public ScienceTargetDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
  }

  @Override
  public void onPlayerUpdate(ArtemisPlayer player) {
    super.onPlayerUpdate(player);
    int id = player.getScienceTarget();

    if (id != -1) {
      setTargetId(id);
    }
  }
}
