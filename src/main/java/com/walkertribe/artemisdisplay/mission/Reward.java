package com.walkertribe.artemisdisplay.mission;

/**
 * Represents the possible rewards from missions.
 * @author rjwut
 */
public enum Reward {
  NONE("no reward"),
  COOLANT("coolant", "give you more engineering coolant"),
  NUKES("nukes", "give you two more nuclear torpedoes"),
  ENERGY("energy", "charge your batteries"),
  ENHANCE_SHIELDS("shields", "enhance your shield generators"),
  DOUBLE_PRODUCTION_SPEED("prod speed", "double our production speed"),
  PROBES("probes"),
  UNKNOWN("unknown");

  private static final Reward[] ALL = Reward.values();

  /**
   * Returns the Reward that corresponds to the given token, or null if doesn't match any.
   */
  static Reward extract(String token) {
    for (Reward reward : ALL) {
      if (reward.token != null && reward.token.equals(token)) {
        return reward;
      }
    }

    return null;
  }

  private String label;
  private String token;

  /**
   * Create a Reward with no token.
   */
  private Reward(String label) {
    this.label = label;
  }

  /**
   * Creates a Reward from a token present in the COMM message that offers the Mission.
   */
  private Reward(String label, String token) {
    this.label = label;
    this.token = token;
  }

  @Override
  public String toString() {
    return label;
  }
}
