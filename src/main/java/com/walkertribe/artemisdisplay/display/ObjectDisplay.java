package com.walkertribe.artemisdisplay.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.Canvas;
import com.walkertribe.artemisdisplay.TextFitter;
import com.walkertribe.artemisdisplay.render.DataSheet;
import com.walkertribe.artemisdisplay.render.Meter;
import com.walkertribe.artemisdisplay.render.PolygonBuilder;
import com.walkertribe.artemisdisplay.render.RenderMode;
import com.walkertribe.artemisdisplay.render.TopDownObjectRenderer;
import com.walkertribe.artemisdisplay.util.Angle;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.world.ArtemisObject;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * Abstract class capable of rendering the status of a single ArtemisObject.
 * @author rjwut
 */
public abstract class ObjectDisplay extends AbstractDisplay {
  private static final int NO_TARGET = 0;
  private static final double MODEL_DISPLAY_WIDTH = 0.5f;

  private int targetId = NO_TARGET;
  private TextFitter textFitter;
  private TopDownObjectRenderer renderer;

  public ObjectDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
    textFitter = Canvas.getTextFitter(false).padding(0.1f);
    renderer = new TopDownObjectRenderer(ctx, app.getWorld(), RenderMode.SOLID_WIREFRAME, false);
  }

  @Override
  public void reset() {
    targetId = 1;
  }

  /**
   * Returns the ArtemisObject to be displayed.
   */
  protected ArtemisObject getTarget() {
    return targetId == 1 ? null : app.getWorld().get(targetId);
  }

  /**
   * Sets the ID of the ArtemisObject to be displayed. To clear the target, pass in 1.
   */
  protected void setTargetId(int id) {
    targetId = id;
  }

  @Override
  protected void renderImpl(Graphics2D g) {
    Rectangle bounds = g.getClipBounds();

    if (targetId == NO_TARGET) {
      // No target selected
      g.setColor(Color.DARK_GRAY);
      textFitter.render(g, bounds, localeData.string("target.none"));
      return;
    }

    double modelDisplayWidth = bounds.width * MODEL_DISPLAY_WIDTH;
    double cx = modelDisplayWidth / 2 + bounds.x;
    double cy = bounds.getCenterY();
    double r = Math.min(cx - bounds.x, cy - bounds.y);
    ArtemisObject target = getTarget();

    if (target != null) {
      ArtemisPlayer player = getPlayer();
      renderer.render(g, cx, cy, 0.5 * r, target, player);

      if (player != null) {
        if (targetId != player.getId()) {
          renderBearing(g, target, player, cx, cy, r);
          renderRange(g, bounds, target, player);

          if (targetId == player.getScanObjectId()) {
            float progress = player.getScanProgress();

            if (!Float.isNaN(progress) && progress < 1) {
              renderScan(g, bounds, progress);
            }
          }
        }
      }

      renderData(g, bounds, (int) modelDisplayWidth, target);
    }
  }

  /**
   * Renders the bearing arrow.
   */
  private void renderBearing(Graphics2D g, ArtemisObject target, ArtemisPlayer player, double cx,
      double cy, double r) {
    float dx = player.getX() - target.getX();
    float dz = player.getZ() - target.getZ();
    float t = (float) Math.atan2(dz, dx);
    String degrees = Integer.toString((int) Angle.DEGREES.fromRadians(t));
    t += Math.PI;
    PolygonBuilder builder = new PolygonBuilder(cx, cy);
    double rInner = r * 0.6;
    double tOffset = Math.PI * 0.1;
    builder
      .add(t, rInner)
      .add(t + tOffset, r)
      .add(t - tOffset, r);
    g.setColor(Color.WHITE);
    g.fill(builder.toPath());
    Point2D.Double center = builder.average();
    Font font = localeData.getFont((float) r / 10);
    FontMetrics fontMetrics = g.getFontMetrics(font);
    Rectangle2D stringBounds = fontMetrics.getStringBounds(degrees, g);
    g.setColor(Color.BLACK);
    g.setFont(font);
    float height = (float) (stringBounds.getHeight() - fontMetrics.getDescent());
    g.drawString(
        degrees,
        (float) (center.getX() - stringBounds.getWidth() / 2),
        (float) (center.getY() + height / 2)
    );
  }

  /**
   * Renders the range to the target.
   */
  private void renderRange(Graphics2D g, Rectangle bounds, ArtemisObject target, ArtemisPlayer player) {
    int d = Math.round((float) target.distance(player));
    String key = "target.range." + (d < 1000 ? "near" : "far");
    float value = d < 1000 ? d : d / 1000f;
    String rangeStr = localeData.string(key, value);
    g.setFont(localeData.getFont(0.05f * bounds.height));
    float baseline = (float) bounds.getMaxY() - g.getFontMetrics().getDescent();
    g.setColor(Color.GRAY);
    g.drawString(rangeStr, bounds.x + 3, baseline);
  }

  /**
   * Renders the scan progress meter.
   */
  private void renderScan(Graphics2D g, Rectangle bounds, float progress) {
    g.setFont(localeData.getFont(0.05f * bounds.height));
    String label = localeData.string("target.scan");
    Meter meter = new Meter(label, progress, 1, Meter.ColorScheme.SCAN);
    meter.render(g, new Rectangle(bounds.x, bounds.y + 1, bounds.width / 5, bounds.height / 20));
  }

  /**
   * Renders the data sheet.
   */
  private void renderData(Graphics2D g, Rectangle bounds, int modelDisplayWidth, ArtemisObject target) {
    int dataPanelWidth = bounds.width - modelDisplayWidth;
    int dataPanelHeight = Math.round(bounds.height * 0.9f);
    int x = bounds.x + bounds.width - dataPanelWidth + Math.round(dataPanelWidth * 0.05f);
    int y = bounds.y + Math.round(bounds.height * 0.05f);
    dataPanelWidth *= 0.9f;
    Rectangle dataSheetBounds = new Rectangle(x, y, dataPanelWidth, dataPanelHeight);
    DataSheet sheet = new DataSheet(app, g, target, getPlayer());
    sheet.render(dataSheetBounds);
  }
}
