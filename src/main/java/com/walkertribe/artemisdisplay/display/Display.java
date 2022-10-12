package com.walkertribe.artemisdisplay.display;

import java.awt.Graphics2D;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.iface.ArtemisNetworkInterface;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * A component that can render on a Rectangular area of the Canvas. Layout is a subtype of Display.
 * @author rjwut
 */
public interface Display {
  /**
   * The various display types
   */
  public static enum Type {
    ALERT("Alert status", AlertDisplay.class),
    BASES("Bases status", BasesDisplay.class),
    CAPTAIN_TARGET("Captain target", CaptainTargetDisplay.class),
    LRS("Long range sensors", LrsDisplay.class),
    MISSIONS("Missions", MissionsDisplay.class),
    ORDNANCE("Ordnance", OrdnanceDisplay.class),
    PLAYER_SHIP("Player ship", PlayerShipDisplay.class),
    SCIENCE_TARGET("Science target", ScienceTargetDisplay.class),
    SYSTEMS("System status", SystemsDisplay.class),
    TACTICAL("Tactical view", TacticalDisplay.class),
    TIMER("Timer", TimerDisplay.class),
    TUBES("Tubes", TubesDisplay.class),
    WEAPONS_TARGET("Weapons target", WeaponsTargetDisplay.class);

    private String label;
    private Class<? extends Display> clazz;

    private Type(String label, Class<? extends Display> clazz) {
      this.label = label;
      this.clazz = clazz;
    }

    /**
     * Creates a new instance of this type of Display.
     */
    public Display newInstance(ArtemisDisplay app, Context ctx) {
      try {
        return clazz.getConstructor(ArtemisDisplay.class, Context.class).newInstance(app, ctx);
      } catch (ReflectiveOperationException ex) {
        throw new RuntimeException(ex);
      }
    }

    @Override
    public String toString() {
      return label;
    }
  }

  /**
   * Provides an ArtemisNetworkInterface to which this Display should attach. The Display should
   * do nothing if the given ArtemisNetworkInterface is currently attached. Note that the argument
   * may be null (meaning the Display should be detached).
   */
  void attach(ArtemisNetworkInterface iface);

  /**
   * Tells the Display to render itself on the given Graphics2D context. The bounds of the current
   * clip dictate the render area to which the Display should limit itself. A Display may change the
   * clip to any area within the initial clip, but should change it back before render() is over.
   */
  void render(Graphics2D g);

  /**
   * Notifies the Display that the selected player ship has spawned in the World.
   */
  void onPlayerSpawn(ArtemisPlayer player);

  /**
   * Notifies the Display that the selected player ship has had an update. Only the ship with the
   * selected index will be reported here. If a Display is interested in all player ships, it should
   * have a separate, annotated Listener method that listens for ArtemisPlayer. The given object
   * contains the complete player state, not just the update.
   */
  void onPlayerUpdate(ArtemisPlayer player);

  /**
   * Notifies the Display that the selected player ship has been deleted from the World. This might
   * be because the player was destroyed, or because the simulation ended.
   */
  void onPlayerDelete(ArtemisPlayer player);

  /**
   * The simulation is no longer running or the connection has been lost. Displays should reset
   * their state.
   */
  void reset();

  /**
   * Sets whether the display should have a border.
   */
  void setBorder(boolean border);

  /**
   * Sets the title to display at the top of the display.
   */
  void setTitle(String title);
}
