package com.walkertribe.artemisdisplay.mission;

/**
 * The types of contacts involved with a Mission.
 * @author rjwut
 */
public enum ContactType {
  /**
   * The contact which gave the mission
   */
  GIVER,
  /**
   * The contact the player must interact with before they can collect the reward
   */
  TASK,
  /**
   * The contact which has the player's reward
   */
  REWARD
}
