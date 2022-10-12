package com.walkertribe.artemisdisplay;

/**
 * Static utility class
 * @author rjwut
 */
public final class Util {
  /**
   * <p>
   * Parses a floating point value in any of the following formats:
   * </p>
   * <ul>
   * <li>A standard float value, e.g. "0.25"</li>
   * <li>A percentage value, e.g. "25%"</li>
   * <li>A fraction, e.g. "1/4"</li>
   * </ul>
   * <p>
   * All of the above would return a value of 0.25f.
   * </p>
   */
  public static float parseFloat(String str) {
    float value;
    int slashPos = str.indexOf('/');

    try {
      if (slashPos != -1) {
        // fraction
        float numerator = Float.parseFloat(str.substring(0, slashPos));
        float denominator = Float.parseFloat(str.substring(slashPos +1));

        if (denominator == 0) {
          throw new IllegalArgumentException("Can't divide by zero: " + str);
        }

        value = numerator / denominator;
      } else if (str.endsWith("%")) {
        // percentage
        value = Float.parseFloat(str.substring(0, str.length() - 1)) / 100;
      } else {
        value = Float.parseFloat(str);
      }
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid value: " + str);
    }

    return value;
  }
}
