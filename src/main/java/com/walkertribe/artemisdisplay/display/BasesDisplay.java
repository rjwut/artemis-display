package com.walkertribe.artemisdisplay.display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.SortedSet;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.BaseMap;
import com.walkertribe.artemisdisplay.BaseType;
import com.walkertribe.artemisdisplay.i18n.LocaleData;
import com.walkertribe.artemisdisplay.render.Table;
import com.walkertribe.artemisdisplay.render.Util;
import com.walkertribe.artemisdisplay.util.Angle;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.iface.Listener;
import com.walkertribe.ian.protocol.core.world.DeleteObjectPacket;
import com.walkertribe.ian.util.BoolState;
import com.walkertribe.ian.world.ArtemisBase;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * Shows the status of bases: bearing, distance, shields, and type
 * @author rjwut
 */
public class BasesDisplay extends AbstractDisplay {
  // CellRenderers for each column
  private final Table.CellRenderer<BaseMap.Entry<BaseType>> nameRenderer = new Table.CellRenderer<BaseMap.Entry<BaseType>>() {
    @Override
    public Color getColor(BaseMap.Entry<BaseType> entry) {
      if (!entry.isAlive()) {
        return Color.DARK_GRAY;
      }

      BoolState friendly = entry.isFriendly();

      if (friendly == BoolState.TRUE) {
        return Color.GREEN;
      }

      return friendly == BoolState.FALSE ? Color.RED : Color.LIGHT_GRAY;
    }

    @Override
    public String toString(LocaleData localeData, BaseMap.Entry<BaseType> entry) {
      String name = entry.getDisplayName();
      return name != null ? name : localeData.string("bases.unknown");
    }
  };
  private final Table.CellRenderer<BaseMap.Entry<BaseType>> bearingRenderer = new Table.CellRenderer<BaseMap.Entry<BaseType>>() {
    @Override
    public Color getColor(BaseMap.Entry<BaseType> entry) {
      return entry.isAlive() ? Color.LIGHT_GRAY : Color.DARK_GRAY;
    }

    @Override
    public String toString(LocaleData localeData, BaseMap.Entry<BaseType> entry) {
      if (!entry.isAlive()) {
        return "";
      }

      ArtemisBase base = entry.getBase();
      ArtemisPlayer player = getPlayer();

      if (base == null || player == null) {
        return localeData.string("bases.unknown");
      }

      float dx = player.getX() - base.getX();
      float dz = player.getZ() - base.getZ();
      double bearing = Math.atan2(dz, dx);
      int degrees = (int) Math.round(Angle.DEGREES.fromRadians((float) bearing));
      return localeData.string("bases.degrees", degrees);
    }
  };
  private final Table.CellRenderer<BaseMap.Entry<BaseType>> distanceRenderer = new Table.CellRenderer<BaseMap.Entry<BaseType>>() {
    @Override
    public Color getColor(BaseMap.Entry<BaseType> entry) {
      if (!entry.isAlive()) {
        return Color.DARK_GRAY;
      }

      ArtemisBase base = entry.getBase();
      ArtemisPlayer player = getPlayer();

      if (base == null || player == null) {
        return Color.LIGHT_GRAY;
      }

      float distance = base.distance(player);
      return distance < 600 ? Color.BLUE : Color.LIGHT_GRAY;
    }

    @Override
    public String toString(LocaleData localeData, BaseMap.Entry<BaseType> entry) {
      if (!entry.isAlive()) {
        return "";
      }

      ArtemisBase base = entry.getBase();
      ArtemisPlayer player = getPlayer();

      if (base == null || player == null) {
        return localeData.string("bases.unknown");
      }

      float d = (float) base.distance(player);
      boolean near = d < 1000;
      return localeData.string(
          "bases.range." + (near ? "near" : "far"),
          Math.round(near ? d : d / 1000)
      );
    }
  };
  private final Table.CellRenderer<BaseMap.Entry<BaseType>> shieldsRenderer = new Table.CellRenderer<BaseMap.Entry<BaseType>>() {
    @Override
    public Color getColor(BaseMap.Entry<BaseType> entry) {
      if (!entry.isAlive()) {
        return Color.DARK_GRAY;
      }

      ArtemisBase base = entry.getBase();

      if (base == null) {
        return Color.LIGHT_GRAY;
      }

      float shields = Math.max(base.getShieldsFront(), 0);
      return Util.computeShieldColor(shields, base.getShieldsFrontMax());
    }

    @Override
    public String toString(LocaleData localeData, BaseMap.Entry<BaseType> entry) {
      if (!entry.isAlive()) {
        return "";
      }

      ArtemisBase base = entry.getBase();

      if (base == null) {
        return localeData.string("bases.unknown");
      }

      float shields = base.getShieldsFront();
      float shieldsMax = base.getShieldsFrontMax();

      if (Float.isNaN(shields) || Float.isNaN(shieldsMax)) {
        return localeData.string("bases.unknown");
      }

      return localeData.formatPercent(Math.max(shields / shieldsMax, 0));
    }
  };
  private final Table.CellRenderer<BaseMap.Entry<BaseType>> typeRenderer = new Table.CellRenderer<BaseMap.Entry<BaseType>>() {
    @Override
    public Color getColor(BaseMap.Entry<BaseType> entry) {
      return entry.isAlive() ? Color.LIGHT_GRAY : Color.DARK_GRAY;
    }

    @Override
    public String toString(LocaleData localeData, BaseMap.Entry<BaseType> entry) {
      BaseType type = entry.getData();

      if (type == null) {
        return localeData.string("bases.unknown");
      }

      return localeData.string("bases.type", type);
    }
  };

  private BaseMap<BaseType> baseMap;
  private Table<BaseMap.Entry<BaseType>> table;

  public BasesDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
    baseMap = new BaseMap<>();
    table = new Table<BaseMap.Entry<BaseType>>()
      .column("", nameRenderer, 3)
      .column(localeData.string("bases.bearing"), bearingRenderer)
      .column(localeData.string("bases.distance"), distanceRenderer)
      .column(localeData.string("bases.shields"), shieldsRenderer)
      .column(localeData.string("bases.type"), typeRenderer);
  }

  @Listener
  public void onBase(ArtemisBase update) {
    BaseMap.Entry<BaseType> entry = baseMap.offer(update, getPlayer());

    if (entry.getData() == null && ctx != null) {
      entry.setData(BaseType.detectBaseType(entry.getBase().getVessel(ctx)));
    }
  }

  @Listener
  public void onDestroyed(DeleteObjectPacket pkt) {
    baseMap.offer(pkt);
  }

  @Override
  public void reset() {
    baseMap.clear();
  }

  @Override
  protected void renderImpl(Graphics2D g) {
    SortedSet<BaseMap.Entry<BaseType>> bases = baseMap.entries();
    table.body().rows(bases).render(g);
  }
}
