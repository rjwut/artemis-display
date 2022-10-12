package com.walkertribe.artemisdisplay.mission;

import java.util.regex.Matcher;

import com.walkertribe.ian.world.BaseArtemisShielded;
import com.walkertribe.ian.world.World;

/**
 * The possible types of Missions.
 */
public enum MissionType {
  /**
   * Pick up something from the task contact, then deliver it to another contact.
   */
  COURIER(
      new Step(ContactType.TASK, Message.COURIER_PICKUP),
      new Step(ContactType.REWARD, Message.COURIER_COMPLETE)
  ) {
    @Override
    protected Mission extractMission(World world, BaseArtemisShielded sender, String msg) {
      Matcher matcher = Message.COURIER_INVITATION.matcher(msg);

      if (matcher.find()) {
        Mission mission = new Mission(this, sender, Reward.extract(matcher.group(2)));
        mission.add(world, ContactType.TASK, matcher.group(1));
        mission.add(ContactType.REWARD, sender);
        return mission;
      }

      return null;
    }
  },

  /**
   * Contact's engines are damaged; they ask for a DAMCON team member.
   */
  DAMAGED_ENGINES(
      Message.REWARD_DELIVERED_TO_STATION,
      new Step(ContactType.GIVER, Message.DAMAGED_ENGINES_COMPLETE)
  ) {
    @Override
    protected Mission extractMission(World world, BaseArtemisShielded sender, String msg) {
      if (Message.DAMAGED_ENGINES_INVITATION.matcher(msg).find()) {
        Mission mission = new Mission(this, sender, Reward.NUKES);
        return mission;
      }

      return null;
    }
  },

  /**
   * No task to perform; just meet the mission giver to get some energy.
   */
  ENERGY_OFFER(
      new Step(ContactType.REWARD, Message.ENERGY_COMPLETE)
  ) {
    @Override
    protected Mission extractMission(World world, BaseArtemisShielded sender, String msg) {
      if (Message.ENERGY_INVITATION.matcher(msg).find()) {
        Mission mission = new Mission(this, sender, Reward.ENERGY);
        mission.add(ContactType.REWARD, sender);
        return mission;
      }

      return null;
    }
  },

  /**
   * Ship held hostage with a ransom demand of 900 energy. Meet them with at least 900 energy to
   * complete.
   */
  ENERGY_RANSOM(
      new Step(ContactType.GIVER, Message.ENERGY_RANSOM_COMPLETE)
  ) {
    @Override
    protected Mission extractMission(World world, BaseArtemisShielded sender, String msg) {
      if (Message.ENERGY_RANSOM_INVITATION.matcher(msg).find()) {
        return new Mission(this, sender, Reward.ENHANCE_SHIELDS);
      }

      return null;
    }
  },

  /**
   * Contact sends suspicious message; explodes if you get close.
   */
  EXPLOSION_TRAP(
      Message.FAILURE_ON_STEPS_COMPLETE,
      new Step(ContactType.GIVER, Message.EXPLOSION_TRAP_FAILURE)
  ) {
    @Override
    protected Mission extractMission(World world, BaseArtemisShielded sender, String msg) {
      if (Message.EXPLOSION_TRAP_INVITATION.matcher(msg).find()) {
        Mission mission = new Mission(this, sender, Reward.NONE);
        return mission;
      }

      return null;
    }
  },

  /**
   * Contact sends suspicious message; releases fighters if you get close.
   */
  FIGHTER_TRAP(
      Message.FAILURE_ON_STEPS_COMPLETE,
      new Step(ContactType.GIVER, Message.FIGHTER_TRAP_FAILURE)
  ) {
    @Override
    protected Mission extractMission(World world, BaseArtemisShielded sender, String msg) {
      if (Message.FIGHTER_TRAP_INVITATION.matcher(msg).find()) {
        Mission mission = new Mission(this, sender, Reward.NONE);
        return mission;
      }

      return null;
    }
  },

  /**
   * Contact's sensors are out, needs guidance to base.
   */
  FLYING_BLIND(
      Message.REWARD_DELIVERED_TO_STATION,
      new Step(ContactType.GIVER, Message.FLYING_BLIND_COMPLETE)
  ) {
    @Override
    protected Mission extractMission(World world, BaseArtemisShielded sender, String msg) {
      if (Message.FLYING_BLIND_INVITATION.matcher(msg).find()) {
        Mission mission = new Mission(this, sender, Reward.UNKNOWN);
        return mission;
      }

      return null;
    }
  },

  /**
   * Ship hijacked; approach from within a nebula to complete.
   */
  HIJACKERS(
      new Step(ContactType.GIVER, Message.HIJACKERS_COMPLETE)
  ) {
    @Override
    protected Mission extractMission(World world, BaseArtemisShielded sender, String msg) {
      if (Message.HIJACKERS_INVITATION.matcher(msg).find()) {
        return new Mission(this, sender, Reward.ENHANCE_SHIELDS);
      }

      return null;
    }
  },

  /**
   * Contact is out of energy; player needs to get close with at least 100 energy.
   */
  JUMP_START(
      Message.REWARD_DELIVERED_TO_STATION,
      new Step(ContactType.GIVER, Message.JUMP_START_COMPLETE)
  ) {
    @Override
    protected Mission extractMission(World world, BaseArtemisShielded sender, String msg) {
      if (Message.JUMP_START_INVITATION.matcher(msg).find()) {
        Mission mission = new Mission(this, sender, Reward.UNKNOWN);
        return mission;
      }

      return null;
    }
  },

  /**
   * Contact's computer is malfunctioning; hit them with an EMP.
   */
  REBOOT_COMPUTER(
      Message.REWARD_DELIVERED_TO_STATION,
      new Step(ContactType.GIVER, Message.REBOOT_COMPUTER_COMPLETE)
  ) {
    @Override
    protected Mission extractMission(World world, BaseArtemisShielded sender, String msg) {
      if (Message.REBOOT_COMPUTER_INVITATION.matcher(msg).find()) {
        return new Mission(this, sender, Reward.UNKNOWN);
      }

      return null;
    }
  },

  /**
   * Launch a shuttle to rendezvous with the contact.
   */
  SHUTTLE_RESCUE(
      new Step(ContactType.REWARD, Message.SHUTTLE_RESCUE_COMPLETE)
  ) {
    @Override
    protected Mission extractMission(World world, BaseArtemisShielded sender, String msg) {
      Matcher matcher = Message.SHUTTLE_RESCUE_INVITATION.matcher(msg);

      if (matcher.find()) {
        Reward reward = "our ambassador".equals(matcher.group(1)) ? Reward.PROBES : Reward.NUKES;
        Mission mission = new Mission(this, sender, reward);
        mission.add(ContactType.REWARD, sender);
        return mission;
      }

      return null;
    }
  };

  boolean immediateReward = true;
  boolean failureOnStepsComplete;
  Step[] steps;

  /**
   * Creates a MissionType with the given Steps.
   */
  private MissionType(Step... steps) {
    this.steps = steps;
  }

  private MissionType(int flags, Step... steps) {
    this(steps);
    this.immediateReward = (flags & Message.REWARD_DELIVERED_TO_STATION) == 0;
    this.failureOnStepsComplete = (flags & Message.FAILURE_ON_STEPS_COMPLETE) != 0;
  }

  /**
   * If the given CommsIncomingPacket is an invitation for this MissionType, returns a new Mission
   * object representing it. Otherwise, returns null.
   */
  protected abstract Mission extractMission(World world, BaseArtemisShielded sender, String msg);

  /**
   * Updates the indicated Mission object according to the given message. Returns true if this
   * resulted in a change to the Mission; false otherwise.
   */
  protected boolean offer(Mission mission, BaseArtemisShielded sender, String msg) {
    return steps[mission.stepIndex].offer(mission, sender, msg);
  }
}
