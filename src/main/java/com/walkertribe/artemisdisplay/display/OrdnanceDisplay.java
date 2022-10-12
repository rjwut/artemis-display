package com.walkertribe.artemisdisplay.display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.BaseMap;
import com.walkertribe.artemisdisplay.i18n.LocaleData;
import com.walkertribe.artemisdisplay.render.Table;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.enums.OrdnanceType;
import com.walkertribe.ian.iface.Listener;
import com.walkertribe.ian.protocol.core.comm.CommsIncomingPacket;
import com.walkertribe.ian.protocol.core.world.DeleteObjectPacket;
import com.walkertribe.ian.util.BoolState;
import com.walkertribe.ian.world.ArtemisBase;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * Displays the ordnance available in the player ship stores and at bases.
 */
public class OrdnanceDisplay extends AbstractDisplay {
  private static final Map<String, OrdnanceType> ORDNANCE_NAME_MAP = new HashMap<>();

  static {
    ORDNANCE_NAME_MAP.put("Torpedo", OrdnanceType.TORPEDO);
    ORDNANCE_NAME_MAP.put("Nuke", OrdnanceType.NUKE);
    ORDNANCE_NAME_MAP.put("Mine", OrdnanceType.MINE);
    ORDNANCE_NAME_MAP.put("EMP", OrdnanceType.EMP);
    ORDNANCE_NAME_MAP.put("Pshock", OrdnanceType.PSHOCK);
    ORDNANCE_NAME_MAP.put("Beacon", OrdnanceType.BEACON);
    ORDNANCE_NAME_MAP.put("Probe", OrdnanceType.PROBE);
    ORDNANCE_NAME_MAP.put("Tag", OrdnanceType.TAG);
  }

  private static final String ORDNANCE_REGEX = "(" + ORDNANCE_NAME_MAP.keySet().stream().collect(Collectors.joining("|")) + ")";
  private static final Pattern STORES_PATTERN = Pattern.compile("(\\d+)\\s+of\\s+" + ORDNANCE_REGEX);
  private static final Pattern BUILD_FINISHED_PATTERN = Pattern.compile("^We've produced another " + ORDNANCE_REGEX + ".  We now have (\\d+).$");

  /**
   * Record for the ordnance at a base.
   */
  private class BaseData {
    private Map<OrdnanceType, Integer> stores = new LinkedHashMap<>();

    private BaseData() {
      for (OrdnanceType type : OrdnanceType.values()) {
        stores.put(type, null);
      }
    }
  }

  // Cell renderers for each column
  private final Table.CellRenderer<Object> nameRenderer = new Table.CellRenderer<Object>() {
    @Override
    public Color getColor(Object row) {
      if (row instanceof ArtemisPlayer) {
        return Color.WHITE;
      }

      @SuppressWarnings("unchecked")
      BaseMap.Entry<BaseData> entry = (BaseMap.Entry<BaseData>) row;
      return entry.isAlive() ? Color.LIGHT_GRAY : Color.DARK_GRAY;
    }

    @Override
    public String toString(LocaleData localeData, Object row) {
      if (row instanceof ArtemisPlayer) {
        return localeData.string("ordnance.ship");
      }

      @SuppressWarnings("unchecked")
      BaseMap.Entry<BaseData> entry = (BaseMap.Entry<BaseData>) row;
      String name = entry.getDisplayName();
      return name != null ? name : localeData.string("ordnance.unknown");
    }
  };

  private BaseMap<BaseData> map;
  private Table<Object> table;

  public OrdnanceDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
    map = new BaseMap<>();
    table = new Table<>().column("", nameRenderer, 4);

    for (OrdnanceType type : OrdnanceType.values()) {
      String label = localeData.string("ordnance." + type.name().toLowerCase() + ".short");
      table.column(label, buildOrdnanceCellRenderer(type));
    }
  }

  /**
   * Produces a CellRenderer for the named OrdnanceType.
   */
  private Table.CellRenderer<Object> buildOrdnanceCellRenderer(final OrdnanceType type) {
    return new Table.CellRenderer<Object>() {
      @Override
      public Color getColor(Object row) {
        Integer qty;
        boolean isPlayer = false;

        if (row instanceof ArtemisPlayer) {
          isPlayer = true;
          qty = ((ArtemisPlayer) row).getTorpedoCount(type);

          if (qty == -1) {
            qty = null;
          }
        } else {
          @SuppressWarnings("unchecked")
          BaseMap.Entry<BaseData> entry = (BaseMap.Entry<BaseData>) row;
          BaseData data = entry.getData();

          if (data == null) {
            return Color.DARK_GRAY;
          }

          qty = data.stores.get(type);
        }

        if (qty == null) {
          return Color.DARK_GRAY;
        }

        if (qty > 3) {
          return isPlayer ? Color.WHITE : Color.LIGHT_GRAY;
        }

        return qty == 0 ? Color.RED : Color.YELLOW;
      }

      @Override
      public String toString(LocaleData localeData, Object row) {
        if (row instanceof ArtemisPlayer) {
          int qty = ((ArtemisPlayer) row).getTorpedoCount(type);
          return qty != -1 ? Integer.toString(qty) : localeData.string("ordnance.unknown");
        }

        @SuppressWarnings("unchecked")
        BaseMap.Entry<BaseData> entry = (BaseMap.Entry<BaseData>) row;

        if (!entry.isAlive()) {
          return "";
        }

        BaseData data = entry.getData();

        if (data == null) {
          return localeData.string("ordnance.unknown");
        }

        Integer qty = data.stores.get(type);

        if (qty == null) {
          return localeData.string("ordnance.unknown");
        }

        return Integer.toString(qty);
      }
    };
  }

  @Listener
  public void onBase(ArtemisBase update) {
    BaseMap.Entry<BaseData> entry = map.get(update.getId());
    ArtemisPlayer player = getPlayer();
    byte playerSide = player != null ? player.getSide() : -1;
    boolean create = false;

    if (entry == null) {
      byte updateSide = update.getSide();
      create = updateSide != -1 && updateSide == playerSide;
    } else {
      create = entry.getDisplayName() == null;
    }

    if (create) {
      entry = map.offer(update, player);
    }
  }

  @Listener
  public void onMessage(CommsIncomingPacket pkt) {
    BaseMap.Entry<BaseData> entry = map.get(pkt.getFrom().toString(), true);
    String msg = pkt.getMessage().toString();
    BaseData data = entry.getData();

    if (data == null) {
      data = new BaseData();
      entry.setData(data);
    }

    if (handleStoresMessage(msg, data)) {
      return;
    }

    handleBuildFinishedMessage(msg, data);
  }

  /**
   * We got a message from a base telling us how much they have of each type of ordnance. Update
   * our records.
   */
  private boolean handleStoresMessage(String msg, BaseData data) {
    Matcher matcher = STORES_PATTERN.matcher(msg);
    boolean found = false;

    while (matcher.find()) {
      found = true;
      OrdnanceType type = ORDNANCE_NAME_MAP.get(matcher.group(2));

      if (type != null) {
        data.stores.put(type, Integer.parseInt(matcher.group(1)));
      }
    }

    return found;
  }

  /**
   * A base told us they just finished building ordnance. Update our records.
   */
  private void handleBuildFinishedMessage(String msg, BaseData data) {
    Matcher matcher = BUILD_FINISHED_PATTERN.matcher(msg);

    if (matcher.matches()) {
      OrdnanceType type = ORDNANCE_NAME_MAP.get(matcher.group(1));

      if (type != null) {
        data.stores.put(type, Integer.parseInt(matcher.group(2)));
      }
    }
  }

  @Listener
  public void onDestroyed(DeleteObjectPacket pkt) {
    map.offer(pkt);
  }

  @Override
  public void reset() {
    map.clear();
  }

  @Override
  protected void renderImpl(Graphics2D g) {
    List<Object> rows = new ArrayList<>(map.entries(entry -> entry.isFriendly() != BoolState.FALSE));
    ArtemisPlayer player = getPlayer();

    if (player != null) {
      rows.add(0, player);
    }

    table.body().rows(rows).render(g);
  }
}
