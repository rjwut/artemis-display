package com.walkertribe.artemisdisplay.mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.walkertribe.ian.protocol.core.comm.CommsIncomingPacket;
import com.walkertribe.ian.protocol.core.world.DeleteObjectPacket;
import com.walkertribe.ian.world.ArtemisObject;
import com.walkertribe.ian.world.BaseArtemisShielded;
import com.walkertribe.ian.world.World;

/**
 * Object which tracks side missions.
 */
public class MissionTracker implements Iterable<Mission> {
  /**
   * Interface for objects which wish to be notified of updates to side missions.
   */
  public interface MissionListener {
    void update(Mission mission);
  }

  private World world;
  private Map<Integer, BaseArtemisShielded> contacts = new HashMap<>();
  private List<Mission> active = new ArrayList<>();
  private List<MissionListener> listeners = new ArrayList<>();

  /**
   * Creates a MissionTracker that is attached to the given World.
   */
  public MissionTracker(World world) {
    if (world == null) {
      throw new IllegalArgumentException("You must provide a World object");
    }

    this.world = world;
  }

  /**
   * Registers an object to be notified of updates to Missions.
   */
  public void addListener(MissionListener listener) {
    listeners.add(listener);
  }

  /**
   * Invoked when a CommsIncomingPacket is received.
   */
  public void onMessage(CommsIncomingPacket pkt) {
    CharSequence from = pkt.getFrom();
    BaseArtemisShielded sender = world.getContactByName(from);

    if (sender == null) {
      return; // We haven't gotten that object yet
    }

    String msg = pkt.getMessage().toString();

    // Does the message contain any new mission invites?
    List<Mission> missionUpdates = Mission.extract(world, sender, msg);

    for (Mission mission : missionUpdates) {
      if (!active.contains(mission)) { // some mission invites can be sent multiple times
        active.add(mission);
      }
    }

    if (missionUpdates.isEmpty()) {
      // No new mission invites; check for an update to an existing mission
      for (Mission curMission : active) {
        if (curMission.offer(sender, msg)) { // mission update
          missionUpdates.add(curMission);
          break;
        }
      }
    }

    if (!missionUpdates.isEmpty()) {
      for (Mission mission : missionUpdates) {
        if (mission.getState() != State.PENDING) { // mission archive
          active.remove(mission);
        }

        missionUpdated(mission);
      }
    }
  }

  /**
   * Invoked when an object is deleted. Checks to see if the deleted object was a contact involved
   * in a Mission.
   */
  public void onObjectDeleted(DeleteObjectPacket pkt) {
    // Note that we look this up in our list of existing contacts rather than the World object,
    // because the World object may have already been updated to reflect this deletion.
    ArtemisObject obj = contacts.get(pkt.getTarget());

    if (!(obj instanceof BaseArtemisShielded)) {
      return; // not a valid contact
    }

    BaseArtemisShielded contact = (BaseArtemisShielded) obj;
    contacts.remove(contact.getId());

    for (Iterator<Mission> iter = active.listIterator(); iter.hasNext(); ) {
      Mission mission = iter.next();

      if (mission.onContactDeleted(contact)) {
        iter.remove();
        missionUpdated(mission);
      }
    }
  }

  /**
   * Returns the number of active missions.
   */
  public int getActiveMissionCount() {
    return active.size();
  }

  /**
   * Clears the MissionTracker state, but leaves it attached to the same World and listeners.
   */
  public void clear() {
    contacts.clear();
    active.clear();
  }

  /**
   * Keeps contacts Map up-to-date and notifies MissionListeners of a Mission update.
   */
  private void missionUpdated(Mission mission) {
    for (BaseArtemisShielded contact : mission.getContacts().values()) {
      contacts.put(contact.getId(), contact);
    }

    listeners.forEach(listener -> listener.update(mission));
  }

  @Override
  public Iterator<Mission> iterator() {
    return active.iterator();
  }
}
