package com.walkertribe.artemisdisplay.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

import com.walkertribe.artemisdisplay.util.Angle;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.enums.CreatureType;
import com.walkertribe.ian.enums.ObjectType;
import com.walkertribe.ian.model.Model;
import com.walkertribe.ian.util.BoolState;
import com.walkertribe.ian.vesseldata.Vessel;
import com.walkertribe.ian.world.ArtemisBase;
import com.walkertribe.ian.world.ArtemisCreature;
import com.walkertribe.ian.world.ArtemisMesh;
import com.walkertribe.ian.world.ArtemisObject;
import com.walkertribe.ian.world.ArtemisOrientable;
import com.walkertribe.ian.world.ArtemisPlayer;
import com.walkertribe.ian.world.ArtemisShielded;
import com.walkertribe.ian.world.World;

/**
 * A class that can render ArtemisObjects in a top-down view.
 * @author rjwut
 */
public class TopDownObjectRenderer {
  private static final int MODEL_RADIUS_THRESHOLD = 7;
  private static final int MIN_ARROW_RADIUS = 10;
  private static final int NEBULA_SCALE = 15;
  private static final Stroke BASE_STROKE = new BasicStroke(2);
  private static final int BLACK_HOLE_SCALE = 28;
  private static final int BLACK_HOLE_RINGS = 5;
  private static final Stroke[] BLACK_HOLE_STROKES = new Stroke[BLACK_HOLE_RINGS];

  static {
    for (int i = 0; i < 5; i++) {
      BLACK_HOLE_STROKES[i] = new BasicStroke(
          1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 5 - i, i }, 0
      );
    }
  }

  private Context ctx;
  private World world;
  private ThreeDModelRenderer modelRenderer;
  private ThreeDRenderParams params = new ThreeDRenderParams();
  private boolean solid;
  private boolean renderNames;
  private Localized localized = new DefaultLocalized();

  /**
   * Creates a new TopDownObjectRenderer that renders objects using Models from the given Context
   * and using the specified RenderMode. If a Locale is provided, then object names will also be
   * rendered.
   */
  public TopDownObjectRenderer(Context ctx, World world, RenderMode renderMode, boolean renderNames) {
    this.ctx = ctx;
    this.world = world;
    modelRenderer = new ThreeDModelRenderer();
    solid = renderMode == RenderMode.SOLID;
    params.renderMode(renderMode);

    if (renderMode != RenderMode.SOLID) {
      params.fillColor(Color.BLACK);
    }

    this.renderNames = renderNames;
  }

  /**
   * Sets the localization handler for this renderer.
   */
  public void setLocalized(Localized localized) {
    this.localized = localized;
  }

  /**
   * Renders an ArtemisObject at the given location and within the indicated radius. 
   */
  public void render(Graphics2D g, double x, double y, double r, ArtemisObject obj,
      ArtemisPlayer player) {
    float heading = Float.NaN;

    if (obj instanceof ArtemisOrientable) {
      heading = ((ArtemisOrientable) obj).getHeading();

      if (!Float.isNaN(heading)) {
        heading = Angle.PROTOCOL.toRadians(heading);
      }
    }

    int scanLevel = -1;

    if (player != null) {
      byte playerSide = player.getSide();
      scanLevel = obj.getScanLevel(playerSide);
    }

    renderObject(g, x, y, r, heading, obj, player, scanLevel);

    if (scanLevel > 0 && r >= MODEL_RADIUS_THRESHOLD) {
      renderShields(g, obj, x, y, r * 1.4, heading + Angle.PI / 2);
    }
  }

  /**
   * Renders the object itself, without shields.
   */
  private void renderObject(Graphics2D g, double x, double y, double r, float heading,
      ArtemisObject obj, ArtemisPlayer player, int scanLevel) {
    Color color = Util.getObjectColor(ctx, world, obj, player, false);
    ObjectType type = obj.getType();

    if (type == ObjectType.BASE) {
      heading = Angle.PI / 2;
    }

    Vessel vessel = null;
    Model model = null;

    if (scanLevel > 0 && r >= MODEL_RADIUS_THRESHOLD && ctx != null) {
      if (obj instanceof ArtemisShielded) {
        ArtemisShielded shielded = (ArtemisShielded) obj;
        vessel = shielded.getVessel(ctx);
        model = vessel.getModel();
      } else {
        model = obj.getModel(ctx);
      }
    }

    if (model != null) {
      if (solid) {
        params.fillColor(color);
      } else {
        params.lineColor(color);
      }

      params
        .offsetX(x)
        .offsetZ(y)
        .scale(model.computeScale(r));

      if (!Float.isNaN(heading)) {
        params.rotateY(heading - Angle.PI / 2);
      }

      if (vessel != null) {
        modelRenderer.render(g, vessel, params, null);
      } else {
        modelRenderer.render(g, model, params, null);
      }
    } else {
      g.setColor(color);

      if (type == ObjectType.BASE) {
        r = MIN_ARROW_RADIUS;
        g.setStroke(BASE_STROKE);
        g.draw(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
      } else if (type == ObjectType.NEBULA) {
        r *= NEBULA_SCALE;
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
      } else if (type == ObjectType.BLACK_HOLE) {
        r *= BLACK_HOLE_SCALE;
        double dr = r / BLACK_HOLE_RINGS;

        for (int i = 0; i < BLACK_HOLE_RINGS; i++) {
          double rn = (i + 1) * dr;
          g.setStroke(BLACK_HOLE_STROKES[i]);
          g.draw(new Ellipse2D.Double(x - rn, y - rn, rn * 2, rn * 2));
        }
      } else if (type == ObjectType.TORPEDO) {
        r = 2;
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
      } else {
        if (!Float.isNaN(heading)) {
          r = Math.max(r, MIN_ARROW_RADIUS);
          PolygonBuilder builder = new PolygonBuilder(x, y);
          builder
            .add(heading, r)
            .add(heading + Math.PI * 0.8, r)
            .add(heading + Math.PI, r * 0.5)
            .add(heading + Math.PI * 1.2, r);
          g.fill(builder.toPath());
        } else {
          g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        }
      }
    }

    if (renderNames) {
      String name = getDisplayName(obj, player);

      if (obj instanceof ArtemisCreature) {
        int playerSide = player != null ? player.getSide() : -1;

        if (playerSide == -1 || obj.getScanLevel(playerSide) < 1) {
          name = "ZZ";
        }
      }

       if (name != null) {
        g.setFont(localized.getFont(g, 10f));
        float labelX = (float) (x - g.getFontMetrics().stringWidth(name) / 2);
        float labelY = (float) (y - r);
        g.setColor(color);
        g.drawString(name, labelX, labelY);
      }
    }
  }

  public String getDisplayName(ArtemisObject obj, ArtemisPlayer player) {
    ObjectType type = obj.getType();

    switch (type) {
    case BASE:
    case GENERIC_MESH:
    case PLAYER_SHIP:
    case NPC_SHIP:
      return obj.getNameString();

    case CREATURE:
      CreatureType creatureType = ((ArtemisCreature) obj).getCreatureType();

      if (creatureType != null) {
        return localized.name(creatureType);
      }

      return localized.unknownCreatureName();

    default:
      return null;
    }
  }

  /**
   * Renders an object's shields, if it has any and they're up.
   */
  private void renderShields(Graphics2D g, ArtemisObject target, double x, double y, double r,
      float heading) {
    if (!(target instanceof ArtemisShielded)) {
      return;
    }

    if (target instanceof ArtemisPlayer) {
      ArtemisPlayer pTarget = (ArtemisPlayer) target;

      if (!BoolState.safeValue(pTarget.getShieldsState())) {
        return;
      }
    }

    ArtemisShielded sTarget = (ArtemisShielded) target;

    if (target instanceof ArtemisBase || target instanceof ArtemisMesh) {
      // One shield
      renderFullShield(g, x, y, r, sTarget.getShieldsFront(), sTarget.getShieldsFrontMax());
    } else {
      // Two shields
      renderHalfShield(g, x, y, -heading, r, sTarget.getShieldsFront(), sTarget.getShieldsFrontMax());
      renderHalfShield(g, x, y, -heading + Angle.PI, r, sTarget.getShieldsRear(), sTarget.getShieldsRearMax());
    }
  }

  /**
   * Renders a single shield (for bases).
   */
  private void renderFullShield(Graphics2D g, double cx, double cy, double r, float power, float max) {
    if (power <= 0) {
      return;
    }

    g.setColor(Util.computeShieldColor(power, max));
    double size = r * 2;
    g.setStroke(new BasicStroke((float) (r * 0.1)));
    g.draw(new Arc2D.Double(cx - r, cy - r, size, size, 0, 360, Arc2D.OPEN));
    g.setStroke(Util.ONE_PIXEL_STROKE);
  }

  /**
   * Renders a half-shield (either fore or aft, for ships).
   */
  private void renderHalfShield(Graphics2D g, double cx, double cy, float heading, double r,
      float power, float max) {
    if (power <= 0) {
      return;
    }

    g.setColor(Util.computeShieldColor(power, max));
    double size = r * 2;
    g.setStroke(new BasicStroke((float) (r * 0.1)));
    boolean hasHeading = !Float.isNaN(heading);
    float h = Angle.PROTOCOL.toRadians(hasHeading ? heading : 0);
    float start = h * 180 / Angle.PI - 85;
    g.draw(new Arc2D.Double(cx - r, cy - r, size, size, start, 170, Arc2D.OPEN));
    g.setStroke(Util.ONE_PIXEL_STROKE);
  }

  /**
   * Interface for objects which can provide localization handling for this renderer.
   */
  public interface Localized {
    /**
     * Returns the Font to use to render object names.
     */
    public Font getFont(Graphics2D g, float size);

    /**
     * Returns the name of the given CreatureType in the current Locale.
     */
    public String name(CreatureType type);

    /**
     * Returns the string used to label unknown creatures in the current Locale.
     */
    public String unknownCreatureName();
  }

  /**
   * Default implementation of the Localized interface. This will just use the current Font in the
   * graphics context, the standard English names of creatures, and "ZZ" for unknown creatures.
   */
  private class DefaultLocalized implements Localized {
    @Override
    public Font getFont(Graphics2D g, float size) {
      return g.getFont().deriveFont(size);
    }

    @Override
    public String name(CreatureType type) {
      String name = type.name();
      return name.charAt(0) + name.substring(1).toLowerCase();
    }

    @Override
    public String unknownCreatureName() {
      return "ZZ";
    }
    
  }
}
