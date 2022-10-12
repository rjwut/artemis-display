package com.walkertribe.artemisdisplay.mission;

/**
 * Possible states for a Mission.
 * @author rjwut
 */
public enum State {
  /**
   * Mission is not complete.
   */
  PENDING,
  /**
   * Mission has succeeded.
   */
  SUCCESS,
  /**
   * Mission has failed.
   */
  FAILURE
}
