package com.walkertribe.artemisdisplay.display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.render.Meter;
import com.walkertribe.artemisdisplay.render.MeterBank;
import com.walkertribe.artemisdisplay.render.ModelRenderer;
import com.walkertribe.artemisdisplay.render.RenderMode;
import com.walkertribe.artemisdisplay.render.ThreeDModelRenderer;
import com.walkertribe.artemisdisplay.render.ThreeDRenderParams;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.enums.DriveType;
import com.walkertribe.ian.enums.ShipSystem;
import com.walkertribe.ian.iface.Listener;
import com.walkertribe.ian.model.Model;
import com.walkertribe.ian.util.Grid;
import com.walkertribe.ian.util.GridNode;
import com.walkertribe.ian.world.Artemis;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * Display showing a 3D systems display and meters for the health of each system.
 */
public class SystemsDisplay extends AbstractDisplay {
  private static final float MODEL_DISPLAY_WIDTH = 0.5f;
  private static final Color MODEL_FILL = new Color(0, 0, 63);
  private static final int REVOLUTIONS_PER_MINUTE = 5;
  private static final long PERIOD = 60_000 / REVOLUTIONS_PER_MINUTE;

  private Model model;
  private ModelRenderer<ThreeDRenderParams> renderer = new ThreeDModelRenderer();
  private ThreeDRenderParams params = new ThreeDRenderParams();

  public SystemsDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
    params.renderMode(RenderMode.SOLID);
    params.fillColor(MODEL_FILL);
    params.gridColor(Color.DARK_GRAY);
    params.rotateX(4 * Math.PI / 3);
  }

  @Listener
  public void onPlayerObjectUpdated(ArtemisPlayer update) {
    if (model == null && ctx != null) {
      Model newModel = update.getModel(ctx);

      if (newModel != null) {
        model = newModel;
      }
    }
  }

  @Override
  public void reset() {
    model = null;
  }

  @Override
  protected void renderImpl(Graphics2D g) {
    ArtemisPlayer player = getPlayer();

    if (player == null || model == null) {
      return;
    }

    // Render 3D model
    Rectangle bounds = g.getClipBounds();
    float modelDisplayWidth = bounds.width * MODEL_DISPLAY_WIDTH;
    double cx = modelDisplayWidth / 2 + bounds.getMinX();
    double cy = bounds.getCenterY();
    double theta = Math.PI * 2 * (System.currentTimeMillis() % PERIOD) / PERIOD;
    params.offsetX(cx);
    params.offsetZ(cy);
    params.rotateZ(theta);
    double size = Math.min(modelDisplayWidth, bounds.height) * 0.5;
    params.scale(model.computeScale(size));
    Grid grid = app.getGrid();
    renderer.render(g, model, params, grid);

    // Render meters
    float meterPanelWidth = bounds.width - modelDisplayWidth;
    float meterBankHeight = bounds.height * 0.9f;
    int meterBankWidth = Math.round(meterPanelWidth * 0.9f);
    // Meters are three times taller than the spacing between them
    double unitSize = meterBankHeight / (Artemis.SYSTEM_COUNT * 4 - 1);
    double lineSpacing = unitSize * 4;
    double meterHeight = unitSize * 3;
    double y0 = cy - meterBankHeight / 2;
    int x0 = (int) (bounds.x + modelDisplayWidth + meterPanelWidth * 0.05);
    double y = y0;
    Map<ShipSystem, List<GridNode>> map = grid.groupNodesBySystem();
    MeterBank meterBank = new MeterBank(g);

    for (Map.Entry<ShipSystem, List<GridNode>> entry : map.entrySet()) {
      OptionalDouble damage = entry.getValue().stream()
          .mapToDouble(node -> Math.max(node.getDamage(), 0))
          .average();
      float health = 1 - (float) damage.orElse(1);
      ShipSystem sys = entry.getKey();
      String key;

      if (sys == ShipSystem.WARP_JUMP_DRIVE) {
        DriveType driveType = player.getDriveType();
        key = (driveType != null ? driveType.name().toLowerCase() : "warp") + "_drive";
      } else {
        key = sys.name().toLowerCase();
      }

      String label = localeData.string("systems." + key);
      Rectangle meterBounds = new Rectangle(x0, (int) y, (int) meterBankWidth, (int) meterHeight);
      y += lineSpacing;
      meterBank.addMeter(meterBounds, label, health, 1, Meter.ColorScheme.GRADIATED);
    }

    meterBank.render();
  }
}
