package com.walkertribe.artemisdisplay;

import com.walkertribe.ian.enums.FactionAttribute;
import com.walkertribe.ian.vesseldata.Vessel;
import com.walkertribe.ian.vesseldata.VesselAttribute;

/**
 * The types of bases that appear in stock Artemis.
 * @author rjwut
 */
public enum BaseType {
  DEEP_SPACE(400, 1, 0, 1.0f),
  CIVILIAN(400, 0, 0, 0.5f),
  COMMAND(800, 1, 1, 2.0f),
  INDUSTRIAL(400, 0, 0, 3.0f),
  SCIENCE(400, 0, 0, 0.5f),
  ENEMY,
  OTHER;

  /**
   * Attempts to detect what kind of base the given Vessel represents. All enemy bases return ENEMY.
   * Friendly bases are matched against the characteristics of the stock base types (shields, beam
   * and torpedo port count, production coefficient). Civilian and science bases have the same
   * profile, so this method will return SCIENCE if the name has the word "Science" in it;
   * otherwise, it will return CIVILIAN. The method will return OTHER if no match is found. An
   * IllegalArgumentException will be thrown if the given Vessel is not a base.
   */
  public static BaseType detectBaseType(Vessel vessel) {
    if (vessel == null) {
      return OTHER;
    }

    if (!vessel.is(VesselAttribute.BASE)) {
      throw new IllegalArgumentException("Not a base");
    }

    if (vessel.getFaction().is(FactionAttribute.ENEMY)) {
      return ENEMY;
    }

    int shields = vessel.getForeShields();
    int beams = vessel.getBeamPorts().length;
    int torps = vessel.getBaseTorpedoPorts().length;
    float prod = vessel.getProductionCoeff();
    String name = vessel.getName().toLowerCase();

    for (BaseType type : values()) {
      if (type.matches(shields, beams, torps, prod, name)) {
        return type;
      }
    }

    return OTHER;
  }

  private Integer shields;
  private Integer beams;
  private Integer torps;
  private Float prod;

  private BaseType() {
    // do nothing
  }

  private BaseType(int shields, int beams, int torps, float prod) {
    this.shields = shields;
    this.beams = beams;
    this.torps = torps;
    this.prod = prod;
  }

  /**
   * Returns true if the given characteristics match those of this BaseType. This method always
   * returns false for ENEMY and true for OTHER.
   */
  private boolean matches(int shields, int beams, int torps, float prod, String name) {
    if (this == ENEMY) {
      return false;
    }

    if (this == OTHER) {
      return true;
    }

    if (this.shields != shields || this.beams != beams || this.torps != torps) {
      return false;
    }

    if (Math.abs(this.prod - prod) > 0.01f) {
      return false;
    }

    if (this == CIVILIAN && name.contains("science")) {
      return false;
    }

    return true;
  }
}
