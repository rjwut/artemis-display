package com.walkertribe.artemisdisplay.util;

/**
 * <p>
 * Handles the three different angle systems in use:
 * </p>
 * <ul>
 * <li>Java Math functions use radians, normalized from 0 to 2 pi, with zero pointing "right" and
 * values increasing counter-clockwise.</li>
 * <li>Artemis displays use degrees, normalized from 0 to 360 degrees, with zero pointing "up" and
 * values increasing clockwise.</li>
 * <li>The network protocol uses radians, normalized from -pi to pi, with zero pointing "down" and
 * values increasing clockwise.</li>
 * @author rjwut
 */
public enum Angle {
  /**
   * The system used by the Java Math library.
   */
  RADIANS {
    @Override
    public float toRadians(float angle) {
      return normalize(angle);
    }

    @Override
    public float fromRadians(float angle) {
      return normalize(angle);
    }

    @Override
    public float normalize(float angle) {
      return normalizeAngle(angle, 0, TWO_PI);
    }
  },

  /**
   * The system used by the displays.
   */
  DEGREES {
    @Override
    public float toRadians(float angle) {
      return RADIANS.normalize(PI * -angle / 180 + PI / 2);
    }

    @Override
    public float fromRadians(float angle) {
      return normalize(180 * -angle / PI + 90);
    }

    @Override
    public float normalize(float angle) {
      return normalizeAngle(angle, 0, 360);
    }
  },

  /**
   * The system used by the network protocol.
   */
  PROTOCOL {
    @Override
    public float toRadians(float angle) {
      return RADIANS.normalize(-angle - PI / 2);
    }

    @Override
    public float fromRadians(float angle) {
      return normalize(-angle - PI / 2);
    }

    @Override
    public float normalize(float angle) {
      return normalizeAngle(angle, -PI, PI);
    }
  };

  public static final float PI = (float) Math.PI;
  public static final float TWO_PI = (float) (Math.PI * 2);

  /**
   * Converts an angle in this unit to RADIANS.
   */
  public abstract float toRadians(float angle);

  /**
   * Converts an angle from RADIANS to this unit.
   */
  public abstract float fromRadians(float angle);

  /**
   * Normalizes the given angle.
   */
  public abstract float normalize(float angle);

  /**
   * Normalizes the given angle so that it is between the min and max angles.
   */
  private static float normalizeAngle(float angle, float min, float max) {
    float range = max - min;

    if (angle < min) {
      float diff = (float) Math.ceil((min - angle) / range);
      return angle + diff * range;
    } else if (angle >= max) {
      float diff = (float) Math.ceil((angle - max) / range);
      return angle - diff * range;
    }

    return angle;
  }
}
