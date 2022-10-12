package com.walkertribe.artemisdisplay.mission;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.walkertribe.ian.world.BaseArtemisShielded;

/**
 * A step that must be taken to complete a Mission.
 * @author rjwut
 */
class Step {
  private ContactType senderContactType;
  private Pattern messagePattern;
  private ContactType arg;

  /**
   * Creates a Step, specifying the sender and message regex that indicates that the Step is
   * complete.
   */
  Step(ContactType senderContactType, String message) {
    this.senderContactType = senderContactType;
    messagePattern = Pattern.compile(message);
  }

  /**
   * Creates a Step, specifying the sender and message regex that indicates that the Step is
   * complete. This constructor specifies that the regex has a group that must capture a string that
   * matches the name of the given contact.
   */
  Step(ContactType senderContactType, String message, ContactType arg) {
    this(senderContactType, message);
    this.arg = arg;
  }

  /**
   * Returns the contact that must send the message for this Step to be complete.
   */
  BaseArtemisShielded sender(Mission mission) {
    return mission.getContact(senderContactType);
  }

  /**
   * If the given message matches the requirements for this Step to be complete, the indicated
   * Mission will be updated. It is assumed that the Mission's current Step is this Step.
   */
  boolean offer(Mission mission, BaseArtemisShielded sender, String msg) {
    if (mission.getState() != State.PENDING) {
      // Mission is already over.
      return false;
    }

    if (!sender.equals(mission.getContact(senderContactType))) {
      // Message not sent by the expected contact
      return false;
    }

    Matcher matcher = messagePattern.matcher(msg);

    if (!matcher.find()) {
      // Message content doesn't match for this step
      return false;
    }

    if (arg != null && matcher.group(1).equals(mission.getContact(arg).getName())) {
      // This message has a contact argument, and it doesn't match the expected contact
      return false;
    }

    // Step is complete; advance the Mission
    mission.advance();
    return true;
  }
}
