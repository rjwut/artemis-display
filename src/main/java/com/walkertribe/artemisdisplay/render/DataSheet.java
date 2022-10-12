package com.walkertribe.artemisdisplay.render;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Consumer;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.Configuration;
import com.walkertribe.artemisdisplay.i18n.LocaleData;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.enums.AnomalyType;
import com.walkertribe.ian.enums.BeaconMode;
import com.walkertribe.ian.enums.BeamFrequency;
import com.walkertribe.ian.enums.CreatureType;
import com.walkertribe.ian.enums.FactionAttribute;
import com.walkertribe.ian.enums.ObjectType;
import com.walkertribe.ian.enums.OrdnanceType;
import com.walkertribe.ian.enums.ShipSystem;
import com.walkertribe.ian.enums.SpecialAbility;
import com.walkertribe.ian.enums.TargetingMode;
import com.walkertribe.ian.util.BoolState;
import com.walkertribe.ian.util.GridNode;
import com.walkertribe.ian.vesseldata.Faction;
import com.walkertribe.ian.vesseldata.Vessel;
import com.walkertribe.ian.world.ArtemisAnomaly;
import com.walkertribe.ian.world.ArtemisBase;
import com.walkertribe.ian.world.ArtemisCreature;
import com.walkertribe.ian.world.ArtemisNebula;
import com.walkertribe.ian.world.ArtemisNpc;
import com.walkertribe.ian.world.ArtemisObject;
import com.walkertribe.ian.world.ArtemisPlayer;
import com.walkertribe.ian.world.ArtemisShielded;
import com.walkertribe.ian.world.ArtemisTorpedo;

/**
 * A class that knows how to render a data sheet for each of the types of objects. These sheets
 * are used by ObjectDisplay.
 */
public class DataSheet {
  private static final float TITLE_FONT_RATIO = 0.1f;
  private static final float SUBTITLE_FONT_RATIO = 0.05f;
  private static final float DATA_FONT_RATIO = 0.03f;
  private static final Color LABEL_COLOR = Color.LIGHT_GRAY;
  private static final Color DEFAULT_VALUE_COLOR = Color.WHITE;

  private static final Map<ObjectType, Consumer<DataSheet>> BUILDERS = new HashMap<>();

  static {
    BUILDERS.put(ObjectType.ANOMALY, sheet -> {
      ArtemisAnomaly anomaly = (ArtemisAnomaly) sheet.target;
      AnomalyType type = sheet.scanLevel < 1 ? null : anomaly.getAnomalyType();

      if (type == AnomalyType.BEACON) {
        sheet.title = sheet.localeData.string("target.beacon");
        BeaconMode mode = anomaly.getBeaconMode();
        CreatureType creatureType = anomaly.getBeaconType();
        String beaconType;

        if (mode == null && creatureType == null) {
          beaconType = sheet.localeData.string("target.data.unknown_value");
        } else {
          String modeKey = mode != null ? "target.beacon.mode." + mode.name().toLowerCase() : "target.data.unknown_value";
          String creatureKey = creatureType != null ? "creature." + creatureType.name().toLowerCase() : "target.data.unknown_value";
          beaconType = sheet.localeData.string(
              "target.beacon.mode_and_type",
              sheet.localeData.string(modeKey),
              sheet.localeData.string(creatureKey)
          );
        }

        sheet.add(sheet.localeData.string("target.data.type"), beaconType);
      } else {
        sheet.title = sheet.localeData.string("target.anomaly");
        sheet.add(sheet.localeData.string("target.data.type"), type);
      }
    });
    BUILDERS.put(ObjectType.ASTEROID, sheet -> {
      sheet.title = sheet.localeData.string("target.asteroid");
    });
    BUILDERS.put(ObjectType.BASE, sheet -> {
      ArtemisBase base = (ArtemisBase) sheet.target;
      String name = base.getNameString();
      sheet.title = name != null ? name : sheet.localeData.string("target.base.unknown");
      sheet.subtitle = sheet.getVesselType();
      sheet.shieldMeters.addMeter(
          sheet.localeData.string("target.data.shields"),
          base.getShieldsFront(), base.getShieldsFrontMax()
      );
    });
    BUILDERS.put(ObjectType.BLACK_HOLE, sheet -> {
      sheet.title = sheet.localeData.string("target.black_hole");
    });
    BUILDERS.put(ObjectType.CREATURE, sheet -> {
      ArtemisCreature creature = (ArtemisCreature) sheet.target;
      CreatureType type = sheet.scanLevel == 0 ? null : creature.getCreatureType();
      String key = type != null ? "creature." + type.name().toLowerCase() : "target.creature.unknown";
      sheet.title = sheet.localeData.string(key);

      if (creature.isTagged()) {
        sheet.shieldMeters.addMeter(
            sheet.localeData.string("target.data.health"),
            creature.getHealth(), creature.getMaxHealth()
        );
      }
    });
    BUILDERS.put(ObjectType.DRONE, sheet -> {
      sheet.title = sheet.localeData.string("target.drone");
    });
    BUILDERS.put(ObjectType.GENERIC_MESH, sheet -> {
      String name = sheet.target.getNameString();
      sheet.title = name != null ? name : sheet.localeData.string("target.mesh.unknown");
    });
    BUILDERS.put(ObjectType.MINE, sheet -> {
      sheet.title = sheet.localeData.string("target.mine");
    });
    BUILDERS.put(ObjectType.NEBULA, sheet -> {
      sheet.title = sheet.localeData.string("target.nebula");
      ArtemisNebula nebula = (ArtemisNebula) sheet.target;
      byte type = nebula.getNebulaType();
      sheet.add(
          sheet.localeData.string("target.data.type"),
          type != -1 ? ((Byte) type).toString() : sheet.localeData.string("target.data.unknown")
      );
    });
    BUILDERS.put(ObjectType.NPC_SHIP, sheet -> {
      ArtemisNpc npc = (ArtemisNpc) sheet.target;
      String name = npc.getNameString();
      boolean unknownName = name != null;
      sheet.title = unknownName ? name : sheet.localeData.string("target.unknown_name");

      if (sheet.scanLevel > 0) {
        sheet.subtitle = sheet.getVesselType();
        sheet.shieldMeters.addMeter(
            sheet.localeData.string("target.data.shields.forward"),
            npc.getShieldsFront(), npc.getShieldsFrontMax()
        );
        sheet.shieldMeters.addMeter(
            sheet.localeData.string("target.data.shields.aft"),
            npc.getShieldsRear(), npc.getShieldsRearMax()
        );

        if (sheet.ctx != null) {
          Set<SpecialAbility> abilities = npc.getSpecialAbilities(sheet.ctx);

          if (abilities != null) {
            for (SpecialAbility ability : abilities) {
              BoolState on = npc.isUsingSpecialAbility(ability, sheet.ctx);
              String key = on.toValue("special.on", "special.off", "unknown_value");
              sheet.add(
                  sheet.localeData.string("target.data.special" + ability),
                  sheet.localeData.string("target." + key),
                  on.toValue(Color.RED, Color.WHITE, Color.LIGHT_GRAY)
              );
            }
          }
        }

        if (sheet.scanLevel > 1) {
          for (ShipSystem sys : ShipSystem.values()) {
            String key = sys == ShipSystem.WARP_JUMP_DRIVE ? "warp_drive" : sys.name().toLowerCase();
            String label = sheet.localeData.string("systems." + key);
            float damage = npc.getSystemDamage(sys);
            float health = Float.isNaN(damage) ? 1 : (1 - damage);
            sheet.systemMeters.addMeter(label, health, 1);
          }

          for (BeamFrequency freq : BeamFrequency.values()) {
            sheet.freqMeters.addMeter(freq.name(), npc.getShieldFreq(freq), 1);
          }
        }
      } else {
        if (!unknownName) {
          sheet.subtitle = sheet.localeData.string("target.npc.type.unscanned");
        }
      }
    });
    BUILDERS.put(ObjectType.PLAYER_SHIP, sheet -> {
      ArtemisPlayer target = (ArtemisPlayer) sheet.target;
      String name = target.getNameString();
      sheet.title = name != null ? name : sheet.localeData.string("target.unknown_name");
      sheet.subtitle = sheet.getVesselType();
      sheet.shieldMeters.addMeter(
          sheet.localeData.string("target.data.shields.forward"),
          target.getShieldsFront(), target.getShieldsFrontMax()
      );
      sheet.shieldMeters.addMeter(
          sheet.localeData.string("target.data.shields.aft"),
          target.getShieldsRear(), target.getShieldsRearMax()
      );

      if (sheet.player != null && sheet.player.getId() == target.getId()) {
        Map<ShipSystem, List<GridNode>> sysMap = sheet.app.getGrid().groupNodesBySystem();

        for (Map.Entry<ShipSystem, List<GridNode>> entry : sysMap.entrySet()) {
          ShipSystem sys = entry.getKey();
          String key;

          if (sys == ShipSystem.WARP_JUMP_DRIVE) {
            key = target.getDriveType().name().toLowerCase() + "_drive";
          } else {
            key = sys.name().toLowerCase();
          }

          String label = sheet.localeData.string("systems." + key);
          OptionalDouble damage = entry.getValue().stream()
              .mapToDouble(node -> Math.max(node.getDamage(), 0))
              .average();
          float health = 1 - (float) damage.orElse(0);
          sheet.systemMeters.addMeter(label, health, 1);
        }
      }

      TargetingMode beams = target.getTargetingMode();
      sheet.add(
          sheet.localeData.string("target.data.beams"),
          sheet.localeData.string("target.data.beams", beams)
      );
      sheet.add(sheet.localeData.string("target.data.energy"), target.getEnergy());
      Vessel vessel = sheet.ctx != null ? target.getVessel(sheet.ctx) : null;
      Faction faction = vessel != null ? vessel.getFaction() : null;
      boolean jumpMaster = faction != null ? faction.is(FactionAttribute.JUMPMASTER) : false;

      if (jumpMaster) {
        float jumpCooldown = target.getEmergencyJumpCooldown();
        String jumpCharge;
        Color color;

        if (!Float.isNaN(jumpCooldown)) {
          float jumpChargeVal = 1 - jumpCooldown;
          jumpCharge = sheet.localeData.formatPercent(jumpChargeVal);
          color = Util.computeShieldColor(jumpChargeVal, 1);
        } else {
          jumpCharge = sheet.localeData.string("target.data.unknown_value");
          color = Color.LIGHT_GRAY;
        }

        sheet.add(sheet.localeData.string("target.data.combat_jump"), jumpCharge, color);
      }
    });
    BUILDERS.put(ObjectType.TORPEDO, sheet -> {
      ArtemisTorpedo torp = (ArtemisTorpedo) sheet.target;
      sheet.title = sheet.localeData.string("target.torpedo");
      OrdnanceType type = torp.getOrdnanceType();
      String key = type != null ? type.name().toLowerCase() : "unknown";
      sheet.add(
          sheet.localeData.string("target.data.type"),
          sheet.localeData.string("ordnance." + key)
      );
    });
  }

  private ArtemisDisplay app;
  private Context ctx;
  private LocaleData localeData;
  private Graphics2D g;
  private FontMetrics metrics;
  private ArtemisPlayer player;
  private ArtemisObject target;
  private int scanLevel;
  private String title;
  private String subtitle;
  private MeterGroupEntry shieldMeters;
  private MeterGroupEntry freqMeters;
  private MeterGroupEntry systemMeters;
  private List<SimpleEntry> data = new LinkedList<>();
  private Color titleColor;
  private float y;

  public DataSheet(ArtemisDisplay app, Graphics2D g, ArtemisObject target, ArtemisPlayer player) {
    this.app = app;
    Configuration config = app.getConfig();
    ctx = config.getContext();
    localeData = LocaleData.get();
    this.g = g;
    metrics = g.getFontMetrics();
    this.player = player;
    this.target = target;
    shieldMeters = new MeterGroupEntry(
        localeData.string("target.data." + (target instanceof ArtemisCreature ? "health" : "shields")),
        Meter.ColorScheme.GRADIATED
    );
    freqMeters = new MeterGroupEntry(
        localeData.string("target.data.frequencies"),
        Meter.ColorScheme.GRADIATED
    );
    systemMeters = new MeterGroupEntry(
        localeData.string("target.data.systems"),
        target instanceof ArtemisPlayer ? Meter.ColorScheme.GRADIATED : Meter.ColorScheme.GREEN_AT_FULL
    );

    if (player != null) {
      scanLevel = target.getScanLevel(player.getSide());
    }

    this.titleColor = Util.getObjectColor(ctx, app.getWorld(), target, player, true);

    if (target != null) {
      BUILDERS.get(target.getType()).accept(this);
    }
  }

  /**
   * Adds a new entry to the sheet that displays a string value.
   */
  private void add(String label, Object value) {
    data.add(new SimpleEntry(label, value != null ? value.toString() : "-?-"));
  }

  /**
   * Adds a new entry to the sheet that displays an integer value.
   */
  public void add(String label, float value) {
    data.add(new SimpleEntry(label, Float.isNaN(value) ? "-?-" : Integer.toString(Math.round(value))));
  }

  /**
   * Adds a new entry to the sheet that displays a string value in a particular color.
   */
  public void add(String label, String value, Color color) {
    data.add(new SimpleEntry(label, value, color));
  }

  /**
   * Renders the data sheet within the given bounds.
   */
  public void render(Rectangle bounds) {
    if (target == null) {
      return;
    }

    y = bounds.y;
    int height = bounds.height;
    renderLine(height, TITLE_FONT_RATIO, baseline -> {
      g.setColor(titleColor);
      g.drawString(title, bounds.x, baseline);
    });

    if (subtitle != null) {
      renderLine(height, SUBTITLE_FONT_RATIO, baseline -> {
        g.drawString(subtitle, bounds.x, baseline);
      });
    }

    renderLine(height, DATA_FONT_RATIO, baseline -> {});
    metrics = g.getFontMetrics();
    List<AbstractEntry> entries = new LinkedList<>();

    if (!shieldMeters.isEmpty()) {
      entries.add(shieldMeters);
    }

    if (!freqMeters.isEmpty()) {
      entries.add(freqMeters);
    }

    if (!systemMeters.isEmpty()) {
      entries.add(systemMeters);
    }

    entries.addAll(data);
    int labelWidth = 0;

    for (AbstractEntry entry : entries) {
      labelWidth = Math.max(labelWidth, entry.getLabelWidth());
    }

    int labelEndX = bounds.x + labelWidth;
    int valueBeginX = labelEndX + Math.round(bounds.height * DATA_FONT_RATIO);
    int valueWidth = (int) (bounds.getMaxX() - valueBeginX);

    for (AbstractEntry entry : entries) {
      entry.render(labelEndX, valueBeginX, valueWidth);
    }
  }

  /**
   * Renders a single line in the data sheet.
   */
  private void renderLine(int height, float fontRatio, Consumer<Float> renderer) {
    g.setFont(localeData.getFont(height * fontRatio));
    metrics = g.getFontMetrics();
    y += metrics.getAscent();
    renderer.accept(y);
    y += metrics.getDescent();
  }

  /**
   * Determines the vessel type.
   */
  private String getVesselType() {
    if (ctx == null) {
      return "";
    }

    Vessel vessel = null;
    Faction faction = null;

    if (target instanceof ArtemisShielded) {
      vessel = ((ArtemisShielded) target).getVessel(ctx);
      faction = vessel != null ? vessel.getFaction() : null;
    }

    if (faction == null || vessel == null) {
      return localeData.string("target.npc.type.unscanned");
    }

    return localeData.string("target.vessel.race_and_class", faction.getName(), vessel.getName());
  }

  /**
   * An entry is a single row in the data sheet.
   */
  private abstract class AbstractEntry {
    private String label;

    protected AbstractEntry(String label) {
      this.label = label;
    }

    /**
     * Returns the width of this entry's label.
     */
    private int getLabelWidth() {
      return DataSheet.this.metrics.stringWidth(label);
    }

    /**
     * Renders this entry.
     */
    private void render(int labelEndX, int valueBeginX, int valueWidth) {
      float labelBeginX = labelEndX - getLabelWidth();
      DataSheet.this.g.setColor(LABEL_COLOR);
      DataSheet.this.g.drawString(label, labelBeginX, y + DataSheet.this.metrics.getAscent());
      renderValue(valueBeginX, valueWidth);
    }

    /**
     * Renders the value part of this entry.
     */
    protected abstract void renderValue(int x, int width);
  }

  /**
   * A row in the data sheet that renders a group of meters that all use the same color scheme.
   */
  private class MeterGroupEntry extends AbstractEntry {
    private List<MeterData> meterData = new LinkedList<>();
    private Meter.ColorScheme scheme;

    private MeterGroupEntry(String label, Meter.ColorScheme scheme) {
      super(label);
      this.scheme = scheme;
    }

    /**
     * Adds a meter to the group.
     */
    private void addMeter(String label, float value, float max) {
      meterData.add(new MeterData(label, value, max));
    }

    /**
     * Returns whether there are any meters in this group.
     */
    private boolean isEmpty() {
      return meterData.isEmpty();
    }

    /**
     * Renders the bank of meters.
     */
    protected void renderValue(int x, int width) {
      int lineHeight = metrics.getHeight();
      MeterBank bank = new MeterBank(g);

      for (MeterData meter : meterData) {
        Rectangle meterBounds = new Rectangle(
            x,
            (int) y,
            width,
            lineHeight
        );
        bank.addMeter(meterBounds, meter.label, meter.value, meter.max, scheme);
        y += lineHeight + 1;
      }

      bank.render();
      y += lineHeight;
    }
  }

  /**
   * The data required for rendering a single meter.
   */
  private static class MeterData {
    private String label;
    private float value;
    private float max;

    private MeterData(String label, float value, float max) {
      this.label = label;
      this.value = value;
      this.max = max;
    }
  }

  /**
   * A simple label/value entry in the data sheet.
   */
  private class SimpleEntry extends AbstractEntry {
    private String value;
    private Color valueColor;

    private SimpleEntry(String label, String value) {
      super(label);
      this.value = value;
    }

    private SimpleEntry(String label, String value, Color valueColor) {
      this(label, value);
      this.valueColor = valueColor;
    }

    /**
     * Renders the value for this entry.
     */
    protected void renderValue(int x, int width) {
      g.setColor(valueColor != null ? valueColor : DEFAULT_VALUE_COLOR);
      g.drawString(value, x, y + metrics.getAscent());
      y += metrics.getHeight();
    }
  }
}
