package com.walkertribe.artemisdisplay.mission;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.walkertribe.ian.world.BaseArtemisShielded;
import com.walkertribe.ian.world.World;

/**
 * Tracks the status of a single mission.
 * @author rjwut
 */
public class Mission {
  private static final MissionType[] ALL_TYPES = MissionType.values();

  /**
   * Returns a List containing a new Mission object for each mission invitation contained in the
   * given CommsIncomingPacket.
   */
  public static List<Mission> extract(World world, BaseArtemisShielded sender, String msg) {
    List<Mission> missions = new LinkedList<>();

    for (MissionType missionType : ALL_TYPES) {
      Mission mission = missionType.extractMission(world, sender, msg);

      if (mission != null) {
        missions.add(mission);
      }
    }

    return missions;
  }

  private MissionType type;
  private Map<ContactType, BaseArtemisShielded> contacts = new HashMap<>();
  private Reward reward;
  State state = State.PENDING;
  int stepIndex;
  private int hash;

  Mission(MissionType type, BaseArtemisShielded giver, Reward reward) {
    this.type = type;
    contacts.put(ContactType.GIVER, giver);
    this.reward = reward;
    hash = Objects.hash(type, giver);
  }

  /**
   * Returns a MissionType enum value indicating what type of Mission this is.
   */
  public MissionType getType() {
    return type;
  }

  /**
   * Returns the contact of the indicated type for this Mission, or null if it doesn't have a
   * Contact of that type. All Missions have a GIVER contact.
   */
  public BaseArtemisShielded getContact(ContactType contactType) {
    return contacts.get(contactType);
  }

  /**
   * Returns a List containing the Mission contacts in the order in which the player must interact
   * with them.
   */
  public List<BaseArtemisShielded> getOrderedContactList() {
    return Arrays.stream(type.steps).map(step -> step.sender(Mission.this)).collect(Collectors.toList());
  }

  /**
   * Returns a Map containing all contacts for this Mission.
   */
  Map<ContactType, BaseArtemisShielded> getContacts() {
    return contacts;
  }

  /**
   * Adds a contact for this Mission. The contact object is required to have a name.
   */
  Mission add(ContactType type, BaseArtemisShielded contact) {
    if (contact.getName() == null) {
      throw new IllegalArgumentException("Contact object must have a name");
    }

    contacts.put(type, contact);
    return this;
  }

  /**
   * Adds a contact for this Mission.
   */
  Mission add(World world, ContactType type, String contactName) {
    BaseArtemisShielded contact = world.getContactByName(contactName);

    if (contact == null) {
      throw new IllegalArgumentException("Could not find contact: " + contactName);
    }

    contacts.put(type, contact);
    return this;
  }

  /**
   * Returns the index of the current Step.
   */
  public int getStepIndex() {
    return stepIndex;
  }

  /**
   * Returns a Reward enum value that describes the reward given for completing this Mission.
   */
  public Reward getReward() {
    return reward;
  }

  /**
   * Returns true if the reward is received immediately when the Mission is completed. Otherwise,
   * the reward is delivered by the Mission giver to the next base at which it docks.
   */
  public boolean isRewardImmediate() {
    return type.immediateReward;
  }

  /**
   * Returns a State enum value that describes this Mission's current state.
   */
  public State getState() {
    return state;
  }

  /**
   * Updates this Mission according to the given message. Returns true if this resulted in an update
   * to this Mission; false otherwise.
   */
  public boolean offer(BaseArtemisShielded sender, String message) {
    return type.offer(this, sender, message);
  }

  /**
   * Notifies this Mission that the indicated contact has been deleted. Returns true if this
   * resulted in an update to this Mission; false otherwise.
   */
  public boolean onContactDeleted(BaseArtemisShielded contact) {
    int stepCount = type.steps.length;

    for (int i = stepIndex; i < stepCount; i++) {
      if (Objects.equals(contact, type.steps[i].sender(this))) {
        state = type.failureOnStepsComplete ? State.SUCCESS : State.FAILURE;
        return true;
      }
    }

    return false;
  }

  /**
   * Marks the current Step complete.
   */
  void advance() {
    stepIndex++;

    if (stepIndex == type.steps.length) {
      state = type.failureOnStepsComplete ? State.FAILURE : State.SUCCESS;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(this instanceof Mission)) {
      return false;
    }

    Mission that = (Mission) obj;
    return
        type == that.type &&
        contacts.get(ContactType.GIVER).getId() == that.contacts.get(ContactType.GIVER).getId();
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("MISSION ").append(type)
      .append(" - Reward: ").append(reward)
      .append(" - ").append(state);

    for (int i = 0; i < type.steps.length; i++) {
      b.append("\n  [").append(i < stepIndex ? '\u221a' : ' ').append("] ")
      .append(type.steps[i].sender(this).getName());
    }

    return b.toString();
  }
}
