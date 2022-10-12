package com.walkertribe.artemisdisplay.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import com.walkertribe.ian.Context;
import com.walkertribe.ian.enums.FactionAttribute;
import com.walkertribe.ian.enums.ObjectType;
import com.walkertribe.ian.util.BoolState;
import com.walkertribe.ian.vesseldata.Faction;
import com.walkertribe.ian.vesseldata.Vessel;
import com.walkertribe.ian.world.ArtemisBase;
import com.walkertribe.ian.world.ArtemisMesh;
import com.walkertribe.ian.world.ArtemisNebula;
import com.walkertribe.ian.world.ArtemisNpc;
import com.walkertribe.ian.world.ArtemisObject;
import com.walkertribe.ian.world.ArtemisPlayer;
import com.walkertribe.ian.world.ArtemisShielded;
import com.walkertribe.ian.world.World;

/**
 * Rendering utility functions
 */
public class Util {
  public static final Stroke ONE_PIXEL_STROKE = new BasicStroke(1);
  private static final float PI = (float) Math.PI;
  private static final Color SELF_COLOR = new Color(0f, 0.5f, 0f);
  private static final Color PLAYER_SHIP_COLOR = new Color(127, 0, 255);
  private static final Color FRIENDLY_COLOR = new Color(0, 127, 255);
  private static final Color SURRENDERED_COLOR = Color.YELLOW;
  private static final Color UNKNOWN_COLOR = Color.LIGHT_GRAY;
  private static final Color FRIENDLY_BASE_COLOR = Color.YELLOW;
  private static final Color MINE_COLOR = Color.RED;
  private static final Color ENEMY_COLOR  = Color.RED;
  private static final Color BIOMECH_COLOR = Color.ORANGE;
  private static final Color ANOMALY_COLOR = Color.WHITE;
  private static final Color[] NEBULA_SOLID_COLORS = {
      Color.MAGENTA,
      Color.CYAN,
      Color.YELLOW
  };
  private static final Color[] NEBULA_TRANSLUCENT_COLORS = {
      new Color(255, 0, 255, 32),
      new Color(0, 255, 255, 32),
      new Color(255, 255, 0, 32)
  };
  private static final Color TORPEDO_COLOR = Color.RED;
  private static final Color BLACK_HOLE_COLOR = Color.BLUE;
  private static final Color ASTEROID_COLOR = new Color(0.59f, 0.25f, 0f);
  private static final Color CREATURE_COLOR = Color.MAGENTA;
  private static final Color DRONE_COLOR = Color.YELLOW;

  private Util() {
    // prevent instantiation
  }

  /**
   * Returns the Color for the given object.
   */
  public static Color getObjectColor(Context ctx, World world, ArtemisObject obj, ArtemisPlayer player, boolean solid) {
    ObjectType type = obj.getType();
    byte playerSide = player != null ? player.getSide() : -1;
    Vessel vessel;
    Faction faction;

    switch (type) {
    case PLAYER_SHIP:
      return obj.getId() == player.getId() ? SELF_COLOR : PLAYER_SHIP_COLOR;

    case NPC_SHIP:
      ArtemisNpc npc = (ArtemisNpc) obj;

      if (npc.isSurrendered().getBooleanValue()) {
        return SURRENDERED_COLOR;
      }

      vessel = ctx != null ? npc.getVessel(ctx) : null;
      faction = vessel != null ? vessel.getFaction() : null;
      BoolState friendly = isFriendly(npc, player);

      if (npc.getScanLevel(playerSide) < 1 && !friendly.getBooleanValue()) {
        return UNKNOWN_COLOR;
      }

      if (faction != null && faction.is(FactionAttribute.BIOMECH)) {
        return world != null && world.getBiomechRage() > 0 ? ENEMY_COLOR : BIOMECH_COLOR;
      }

      return friendly.toValue(FRIENDLY_COLOR, ENEMY_COLOR, UNKNOWN_COLOR);

    case BASE:
      return Util.isFriendly((ArtemisBase) obj, player)
          .toValue(FRIENDLY_BASE_COLOR, ENEMY_COLOR, UNKNOWN_COLOR);

    case MINE:
      return MINE_COLOR;

    case ANOMALY:
      return ANOMALY_COLOR;

    case NEBULA:
      ArtemisNebula nebula = (ArtemisNebula) obj;
      byte nebulaType = nebula.getNebulaType();

      if (nebulaType < 1 || nebulaType > 255) {
        nebulaType = 1;
      }

      return (solid ? NEBULA_SOLID_COLORS : NEBULA_TRANSLUCENT_COLORS)[nebulaType - 1];

    case TORPEDO:
      return TORPEDO_COLOR;

    case BLACK_HOLE:
      return BLACK_HOLE_COLOR;

    case ASTEROID:
      return ASTEROID_COLOR;

    case GENERIC_MESH:
      ArtemisMesh mesh = (ArtemisMesh) obj;
      return new Color(mesh.getRed(), mesh.getGreen(), mesh.getBlue());

    case CREATURE:
      return CREATURE_COLOR;

    case DRONE:
      return DRONE_COLOR;

    default:
      throw new IllegalArgumentException("Unknown ObjectType: " + type);
    }
  }

  /**
   * Returns a BoolState indicating the known friendliness stance of the target to the observer;
   * returning TRUE if it is friendly, FALSE if it is hostile, and UNKNOWN if friendliness cannot
   * be determined by the observer.
   */
  public static BoolState isFriendly(ArtemisShielded target, ArtemisShielded observer) {
    if (target == null || observer == null) {
      // Can't determine friendliness with a null target or observer
      return BoolState.UNKNOWN;
    }

    byte targetSide = target.getSide();
    byte observerSide = observer.getSide();

    if (targetSide == -1 || observerSide == -1) {
      // We don't know the side for one or both objects, so stance is unknown
      return BoolState.UNKNOWN;
    }

    if (targetSide == observerSide) {
      // Both objects are on the same side, so they're friendly
      return BoolState.TRUE;
    }

    if (target.getScanLevel(observerSide) < 1) {
      // Observer has not scanned the target, so stance is unknown
      return BoolState.UNKNOWN;
    }

    // Target is hostile
    return BoolState.FALSE;
  }

  /**
   * Returns the Color that should be used to render shields.
   */
  public static Color computeShieldColor(float power, float max) {
    boolean known = !Float.isNaN(power) && !Float.isNaN(max);
    float perc = known ? power / max : Float.NaN;

    if (!Float.isNaN(perc)) {
      return Color.getHSBColor(perc * (float) PI / 12, 1, 1);
    }

    return Color.LIGHT_GRAY;
  }

}
