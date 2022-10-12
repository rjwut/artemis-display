package com.walkertribe.artemisdisplay.display;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * Display showing the status of the tactical officer's current target.
 * @author rjwut
 */
public class WeaponsTargetDisplay extends ObjectDisplay {
  public WeaponsTargetDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
  }

  @Override
  public void onPlayerUpdate(ArtemisPlayer player) {
    super.onPlayerUpdate(player);
    int id = player.getWeaponsTarget();

    if (id != -1) {
      setTargetId(id);
    }
  }
}
