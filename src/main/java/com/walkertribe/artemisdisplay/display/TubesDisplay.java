package com.walkertribe.artemisdisplay.display;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.render.Meter.ColorScheme;
import com.walkertribe.artemisdisplay.render.MeterBank;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.enums.OrdnanceType;
import com.walkertribe.ian.enums.TubeState;
import com.walkertribe.ian.vesseldata.Vessel;
import com.walkertribe.ian.world.Artemis;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * Display showing the status of the player ship's torpedo tubes.
 */
public class TubesDisplay extends AbstractDisplay {
  private int tubeCount = -1;

  public TubesDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
  }

  @Override
  public void reset() {
    tubeCount = -1;
  }

  @Override
  protected void renderImpl(Graphics2D g) {
    ArtemisPlayer player = getPlayer();

    if (player == null) {
      return;
    }

    if (tubeCount == -1) {
      Context ctx = app.getConfig().getContext();

      if (ctx != null) {
        Vessel vessel = player.getVessel(ctx);

        if (vessel != null) {
          tubeCount = vessel.getTorepedoTubes().length;
        }
      }
    }

    int meterCount = tubeCount == -1 ? Artemis.MAX_TUBES : tubeCount;
    Rectangle bounds = g.getClipBounds();
    int meterBankHeight = (int) (bounds.height * 0.9f);
    int meterBankWidth = (int) (bounds.width * 0.9f);
    // Meters are three times taller than the spacing between them
    int units = Artemis.MAX_TUBES * 4 - 1;
    int unitSize = meterBankHeight / units;
    int lineSpacing = unitSize * 4;
    int meterHeight = unitSize * 3;
    meterBankHeight = meterCount * lineSpacing - unitSize;
    int x0 = bounds.x + (bounds.width - meterBankWidth) / 2;
    int y0 = bounds.y + (bounds.height - meterBankHeight) / 2;
    int y = y0;
    MeterBank meterBank = new MeterBank(g);

    for (int i = 0; i < meterCount; i++) {
      float progress;
      TubeState state = player.getTubeState(i);
      state = state != null ? state : TubeState.UNLOADED;

      switch (state) {
      case LOADED:
        progress = 1;
        break;
      case UNLOADED:
        progress = 0;
        break;
      case LOADING:
        progress = 1 - player.getTubeCountdown(i) / 15;
        break;
      case UNLOADING:
        progress = player.getTubeCountdown(i) / 15;
        break;
      default:
        progress = Float.NaN;
      }

      String label;

      if (state == TubeState.UNLOADED) {
        label = localeData.string("ordnance.empty");
      } else {
        OrdnanceType contents = player.getTubeContents(i);
        String key = contents != null ? contents.name().toLowerCase() : "unknown";
        label = localeData.string("ordnance." + key);
      }

      Rectangle meterBounds = new Rectangle(x0, y, meterBankWidth, meterHeight);
      meterBank.addMeter(meterBounds, label, progress, 1, ColorScheme.GREEN_AT_FULL);
      y += lineSpacing;
    }

    meterBank.render();
  }
}
