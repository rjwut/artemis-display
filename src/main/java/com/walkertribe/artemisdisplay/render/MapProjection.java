package com.walkertribe.artemisdisplay.render;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.walkertribe.ian.Context;
import com.walkertribe.ian.enums.GameType;
import com.walkertribe.ian.enums.ObjectType;
import com.walkertribe.ian.util.BoolState;
import com.walkertribe.ian.world.Artemis;
import com.walkertribe.ian.world.ArtemisObject;
import com.walkertribe.ian.world.ArtemisPlayer;
import com.walkertribe.ian.world.World;

/**
 * Class responsible for translating between Artemis world coordinates and screen coordinates.
 * @author rjwut
 */
public class MapProjection {
  private static final float MAP_CENTER = Artemis.MAP_SIZE * 0.5f;
  private static final Color SECTOR_GRID_COLOR = Color.BLUE;
  private static final Color NEUTRAL_ZONE_COLOR = new Color(0, 0, 255, 32);
  private static final float OBJECT_SCALE = 200;
  private static final float RENDER_DISTANCE_MULTIPLIER = 28;
  private static final WorldObjectComparator COMPARATOR = new WorldObjectComparator();

  private World world;
  private TopDownObjectRenderer renderer;
  private Mode mode;

  /**
   * Creates a MapProjection that draws the given World, using the given Context.
   */
  public MapProjection(World world, Context ctx, Mode mode) {
    this.world = world;
    renderer = new TopDownObjectRenderer(ctx, world, RenderMode.SOLID, true);
    this.mode = mode;
  }

  /**
   * Renders onto the given Graphics2D context within its clip bounds. The given ArtemisPlayer
   * object is a reference to the player ship.
   */
  public void render(Graphics2D g, ArtemisPlayer player, GameType gameType) {
    Rectangle bounds = g.getClipBounds();
    float cx = (float) bounds.getCenterX();
    float cy = (float) bounds.getCenterY();
    int size = Math.min(bounds.width, bounds.height);
    float scale = mode.displayScale * (float) size / Artemis.MAP_SIZE;
    float fx = mode.getFocusX(player);
    float fz = mode.getFocusZ(player);

    // Render sector grid
    float x0 = translateX(0, fx, scale, cx);
    float y0 = translateY(0, fz, scale, cy);
    float x1 = translateX(Artemis.MAP_SIZE, fx, scale, cx);
    float y1 = translateY(Artemis.MAP_SIZE, fz, scale, cy);
    g.setColor(SECTOR_GRID_COLOR);

    for (int i = 0; i < 6; i++) {
      float coord = Artemis.MAP_SIZE * 0.2f * i;
      float x = translateX(coord, fx, scale, cx);
      float y = translateY(coord, fz, scale, cy);
      g.drawLine((int) x, (int) y0, (int) x, (int) y1);
      g.drawLine((int) x0, (int) y, (int) (x1), (int) y);
    }

    if (gameType == GameType.BORDER_WAR) {
      // Render neutral zone
      float nzx0 = translateX(Artemis.MAP_SIZE * 0.6f, fx, scale, cx);
      float nzx1 = translateX(Artemis.MAP_SIZE * 0.4f, fx, scale, cx);
      g.setColor(NEUTRAL_ZONE_COLOR);
      g.fillRect((int) nzx0, (int) y0, (int) (nzx1 - nzx0), (int) (y1 - y0));
    }

    if (mode.renderSectorIds) {
      // Render sector IDs
      g.setFont(g.getFont().deriveFont(bounds.height * 0.02f));
      FontMetrics metrics = g.getFontMetrics();
      int dy = metrics.getAscent();

      for (int r = 0; r < 5; r++) {
        for (int c = 0; c < 5; c++) {
          float x = translateX(Artemis.MAP_SIZE * 0.2f * (c + 1), fx, scale, cx) + 3;
          float y = translateY(Artemis.MAP_SIZE * 0.2f * r, fz, scale, cy) + dy;
          g.drawString(Character.toString(('A' + r)) + (5 - c), x, y);
        }
      }
    }

    // Render objects
    for (ArtemisObject obj : world.getAll(COMPARATOR)) {
      if (!obj.hasPosition()) {
        continue; // can't render an object if we don't know where it is
      }

      if (player != null) {
        byte side = player.getSide();

        if (side != -1 && obj.getVisibility(side) == BoolState.FALSE) {
          continue; // object is not visible to us
        }
      }

      float x = translateX(obj.getX(), fx, scale, cx);
      float y = translateY(obj.getZ(), fz, scale, cy);
      float r = scale * OBJECT_SCALE;
      float maxSize = r * RENDER_DISTANCE_MULTIPLIER;
      Rectangle objectBounds = new Rectangle(
          (int) (x - maxSize),
          (int) (y - maxSize),
          (int) (maxSize * 2),
          (int) (maxSize * 2)
      );

      if (bounds.intersects(objectBounds)) {
        renderer.render(g, x, y, r, obj, player);
      }
    }
  }

  /**
   * Translates the given X-coordinate to a screen X-coordinate, given the X-coordinate of the focus
   * point and the scaling factor.
   */
  private float translateX(float x, float fx, float scale, float cx) {
    return scale * (fx - x) + cx;
  }

  /**
   * Translates the given Z-coordinate to a screen Y-coordinate, given the Z-coordinate of the focus
   * point and the scaling factor.
   */
  private float translateY(float z, float fz, float scale, float cy) {
    return scale * (z - fz) + cy;
  }

  /**
   * Render modes
   */
  public enum Mode {
    /**
     * Long range sensor mode: focus on the center, full-map scale, render sector IDs, don't render
     * beam arcs.
     */
    LRS(1, true) {
      @Override
      protected float getFocusX(ArtemisPlayer player) {
        return MAP_CENTER;
      }

      @Override
      protected float getFocusZ(ArtemisPlayer player) {
        return MAP_CENTER;
      }
    },
    /**
     * Tactical mode: focus on player ship, 10x map scale, don't render sector IDs, render beam
     * arcs.
     */
    TACTICAL(10, false) {
      @Override
      protected float getFocusX(ArtemisPlayer player) {
        return player != null ? player.getX() : Artemis.MAP_SIZE / 2;
      }

      @Override
      protected float getFocusZ(ArtemisPlayer player) {
        return player != null ? player.getZ() : Artemis.MAP_SIZE / 2;
      }
    };

    /**
     * Returns the X-coordinate of the focus point for the MapProjection.
     */
    protected abstract float getFocusX(ArtemisPlayer player);

    /**
     * Returns the Z-coordinate of the focus point for the MapProjection.
     */
    protected abstract float getFocusZ(ArtemisPlayer player);

    private float displayScale;
    private boolean renderSectorIds;

    /**
     * Render parameters for the MapProjection. The displayScale controls the scale of the map
     * relative to the size of the Display. A value of 1 means that the map is scaled so that it
     * fits exactly within the Display (if it is centered). Higher values will cause the map to be
     * "zoomed" in by that factor. The other arguments toggle additional map features.
     */
    private Mode(float displayScale, boolean renderSectorIds) {
      this.displayScale = displayScale;
      this.renderSectorIds = renderSectorIds;
    }
  }

  private static class WorldObjectComparator implements Comparator<ArtemisObject> {
    private static final List<ObjectType> TYPE_PRIORITY = new ArrayList<>();

    static {
      TYPE_PRIORITY.add(ObjectType.NEBULA);
      TYPE_PRIORITY.add(ObjectType.BLACK_HOLE);
      TYPE_PRIORITY.add(ObjectType.GENERIC_MESH);
      TYPE_PRIORITY.add(ObjectType.ASTEROID);
      TYPE_PRIORITY.add(ObjectType.ANOMALY);
      TYPE_PRIORITY.add(ObjectType.DRONE);
      TYPE_PRIORITY.add(ObjectType.TORPEDO);
      TYPE_PRIORITY.add(ObjectType.MINE);
      TYPE_PRIORITY.add(ObjectType.BASE);
      TYPE_PRIORITY.add(ObjectType.CREATURE);
      TYPE_PRIORITY.add(ObjectType.NPC_SHIP);
      TYPE_PRIORITY.add(ObjectType.PLAYER_SHIP);
    }

    @Override
    public int compare(ArtemisObject o1, ArtemisObject o2) {
      if (o1 == o2) {
        return 0;
      }

      if (o1 == null) {
        return 1;
      }

      if (o2 == null) {
        return -1;
      }

      int type1 = TYPE_PRIORITY.indexOf(o1.getType());
      int type2 = TYPE_PRIORITY.indexOf(o2.getType());
      int c = type1 - type2;

      if (c == 0) {
        c = o1.getId() - o2.getId();
      }

      return c;
    }
  }
}
