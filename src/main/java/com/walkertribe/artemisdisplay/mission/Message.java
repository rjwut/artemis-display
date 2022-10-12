package com.walkertribe.artemisdisplay.mission;

import java.util.regex.Pattern;

/**
 * Regexes for messages sent from contacts that involve missions.
 * @author rjwut
 */
final class Message {
  static final int REWARD_DELIVERED_TO_STATION = 1;
  static final int FAILURE_ON_STEPS_COMPLETE = 2;

  static final Pattern COURIER_INVITATION = Pattern.compile("^Help us help you.\\nFirst, (?:dock|rendezvous) with (\\S+) and (?:pick up some supplies|download some data) we need. Then, (?:dock|rendezvous) with us, and we'll (.*)\\.$");
  static final String COURIER_PICKUP = "^Transfer complete, .+\\. Please proceed to (\\S+) to deliver the (?:data|supplies)\\.$";
  static final String COURIER_COMPLETE = "^Transfer complete, .+\\.  Thanks for your help!$";

  static final Pattern DAMAGED_ENGINES_INVITATION = Pattern.compile("Our engines are damaged and we can't move!  Could you spare a DamCon team to help us\\?");
  static final String DAMAGED_ENGINES_COMPLETE = "^Thanks for the team, \\S+\\.  We'll proceed to the station, and drop off your reward when we get there\\.$";

  static final Pattern ENERGY_INVITATION = Pattern.compile(" We also have energy to spare, if you need some\\.$");
  static final String ENERGY_COMPLETE = "^Here's the energy we promised you, \\S+\\.  Good luck!$";

  static final Pattern ENERGY_RANSOM_INVITATION = Pattern.compile("We are holding this ship hostage!  Bring us 900 energy, and no tricks, or we blow this ship into dust!!");
  static final String ENERGY_RANSOM_COMPLETE = "^This is the captain, the REAL captain\\.  Those criminals left, using some sort of device, \\S+\\.  Thank you for your assistance\\.  Let us upgrade your shield generators\\.$";

  static final Pattern EXPLOSION_TRAP_INVITATION = Pattern.compile("We're just moving cargo between the stations in this sector\\.  That's all\\.  How are you\\?");
  static final String EXPLOSION_TRAP_FAILURE = "Ha ha!  You've fallen into the trap, \\S+!";

  static final Pattern FIGHTER_TRAP_INVITATION = Pattern.compile("We're broken down!  Out of energy!  Yeah!  Could you come help us\\?");
  static final String FIGHTER_TRAP_FAILURE = "Ha ha!  You've fallen into our trap, \\S+!";

  static final Pattern FLYING_BLIND_INVITATION = Pattern.compile("Our sensors are all down!   We're flying blind\\.  Please guide us to a station for repairs!");
  static final String FLYING_BLIND_COMPLETE = "Thanks for the assist!  We're repaired and ready to proceed on our own now\\.  Enjoy your reward!";

  static final Pattern HIJACKERS_INVITATION = Pattern.compile("We have commandeered this ship!  Make no attempt to stop us\\.  If we see you approach, we will blow this ship into dust!!");
  static final String HIJACKERS_COMPLETE = "^This is the captain, the REAL captain\\.  We have the hijackers in custody now, \\S+  Thank you for your assistance\\.  Let us upgrade your shield generators\\.$";

  static final Pattern JUMP_START_INVITATION = Pattern.compile("We're out of energy!  Could you lend us 100 units to get moving\\?");
  static final String JUMP_START_COMPLETE = "^Thanks for the jump, \\S+\\.  We'll proceed to the station, and drop off your reward when we get there\\.$";

  static final Pattern REBOOT_COMPUTER_INVITATION = Pattern.compile("^Our shipboard computer has gone haywire!  We can't reboot it!  Please help!$");
  static final String REBOOT_COMPUTER_COMPLETE = "^Our systems are rebooting!  Thanks!  We'll proceed to the station, and drop off your reward when we get there\\.$";

  static final Pattern SECRET_DATA_INVITATION = Pattern.compile("We are carrying secret, secure data for your government\\.  Please clear our way to exit the sector\\.");

  static final Pattern SHUTTLE_RESCUE_INVITATION = Pattern.compile("We're dead in space, our docking ports are destroyed, and (our ambassador|the big boss) needs to be (?:rescued|elsewhere)\\.  Can you collect him in your shuttle\\?");
  static final String SHUTTLE_RESCUE_COMPLETE = "^Thanks for (?:rescuing our ambassador|picking up the boss), .+  (?:As a good will gesture, here's a couple of nukes\\.  Put 'em ta good use!  Ha ha ha!|Please, have these probes\\.  Thanks again!)$";

  static final Pattern STATION_SUPPLIES_INVITATION = Pattern.compile("We are carrying needed supplies to the station\\. Please clear our way\\.");
}
