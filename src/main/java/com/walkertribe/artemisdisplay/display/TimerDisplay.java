package com.walkertribe.artemisdisplay.display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.Canvas;
import com.walkertribe.artemisdisplay.TextFitter;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.world.ArtemisPlayer;

/**
 * Displays elapsed time since the start of the simulation.
 */
public class TimerDisplay extends AbstractDisplay {
  private TextFitter textFitter;
  private long startTime = -1;

  public TimerDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
    textFitter = Canvas.getTextFitter(true).padding(0.1f);
  }

  @Override
  public void onPlayerSpawn(ArtemisPlayer player) {
    if (startTime == -1) {
      startTime = System.currentTimeMillis();
    }
  }

  @Override
  protected void renderImpl(Graphics2D g) {
    if (startTime == -1) {
      return;
    }

    int seconds = (int) (System.currentTimeMillis() - startTime) / 1000;
    int minutes = (int) Math.floor(seconds / 60);
    seconds -= minutes * 60;
    StringBuilder b = new StringBuilder();

    if (minutes < 10) {
      b.append('0');
    }

    b.append(minutes).append(':');

    if (seconds < 10) {
      b.append('0');
    }

    b.append(seconds);
    String txt = b.toString();
    Rectangle bounds = g.getClipBounds();
    g.setColor(Color.WHITE);
    textFitter.render(g, bounds, txt);
  }

  @Override
  public void reset() {
    startTime = -1;
  }
}
