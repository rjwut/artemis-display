package com.walkertribe.artemisdisplay.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.Canvas;
import com.walkertribe.artemisdisplay.TextFitter;
import com.walkertribe.artemisdisplay.i18n.LocaleData;
import com.walkertribe.artemisdisplay.mission.Mission;
import com.walkertribe.artemisdisplay.mission.MissionTracker;
import com.walkertribe.artemisdisplay.mission.MissionType;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.iface.Listener;
import com.walkertribe.ian.protocol.core.comm.CommsIncomingPacket;
import com.walkertribe.ian.protocol.core.world.DeleteObjectPacket;

/**
 * Displays available missions as they come in on COMMs.
 */
public class MissionsDisplay extends AbstractDisplay {
  private static final Color ENABLED_COLOR = Color.BLACK;
  private static final Color DISABLED_COLOR = new Color(0, 0, 0, 0.5f);

  private MissionTracker tracker;
  private TextFitter textFitter;

  public MissionsDisplay(ArtemisDisplay app, Context ctx) {
    super(app, ctx);
    tracker = new MissionTracker(app.getWorld());
    textFitter = Canvas.getTextFitter(false).padding(0.1f);
  }

  @Listener
  public void onComms(CommsIncomingPacket pkt) {
    tracker.onMessage(pkt);
  }

  @Listener
  public void onObjectDeleted(DeleteObjectPacket pkt) {
    tracker.onObjectDeleted(pkt);
  }

  @Override
  protected void renderImpl(Graphics2D g) {
    if (tracker.getActiveMissionCount() == 0) {
      // No missions
      g.setColor(Color.DARK_GRAY);
      textFitter.render(g, g.getClipBounds(), localeData.string("missions.none"));
      return;
    }

    // Group missions into categories
    Map<Category, List<Mission>> categories = new LinkedHashMap<>();

    for (Category category : Category.values()) {
      categories.put(category, new LinkedList<>());
    }

    tracker.forEach(mission -> {
      Category category = mission.getType() == MissionType.ENERGY_OFFER ? Category.OFFERING_ENERGY : Category.MISSIONS;
      categories.get(category).add(mission);
    });

    // Compute positions
    Rectangle bounds = g.getClipBounds();
    Font headerFont = localeData.getFont(bounds.height * 0.06f);
    Font contactFont = localeData.getFont(bounds.height * 0.05f);
    FontMetrics headerMetrics = g.getFontMetrics(headerFont);
    int indent = (int) (bounds.width * 0.05);
    int x1 = bounds.x + indent;
    int height = headerMetrics.getDescent();
    int y = bounds.y + headerMetrics.getHeight();
    int x2 = bounds.x + bounds.width - indent;
    int gutter = (int) (headerMetrics.getHeight() * 0.2);

    // Render categories
    for (Map.Entry<Category, List<Mission>> entry : categories.entrySet()) {
      List<Mission> missions = entry.getValue();

      if (missions.isEmpty()) {
        continue;
      }

      Category category = entry.getKey();
      g.setFont(headerFont);
      g.setColor(category.color);
      String categoryName = localeData.string("missions", category);
      g.drawString(categoryName, x1, y);
      y += height;
      g.setColor(Color.WHITE);
      int x = x1;
      TileBuilder builder = new TileBuilder(g, contactFont, localeData);
      int lastTileHeight = 0;

      // Render missions in this category
      for (Mission mission : entry.getValue()) {
        Tile tile = builder.buildTile(category, mission);

        if (x + tile.width > x2) {
          x = x1;
          y += builder.height + gutter;
        }

        tile.render(g, x, y);
        x += tile.width + gutter;
        lastTileHeight = builder.height;
      }

      y += lastTileHeight + gutter * 8;
    }
  }

  /**
   * Object that is responsible for computing the sizes of Tiles and rendering them.
   */
  private static class TileBuilder {
    private Font contactFont;
    private FontMetrics contactMetrics;
    private int height;
    private int padding;
    private int arrowWidth;
    private LocaleData localeData;

    /**
     * Creates a TileBuilder that renders on the given graphics context, using the indicated Fonts.
     */
    private TileBuilder(Graphics2D g, Font contactFont, LocaleData localeData) {
      this.contactFont = contactFont;
      this.contactMetrics = g.getFontMetrics(contactFont);
      height = (int) (contactMetrics.getAscent() * 1.1 - contactMetrics.getDescent());
      padding = (int) (height * 0.3);
      height += padding * 2;
      arrowWidth = contactMetrics.stringWidth(" > ");
      this.localeData = localeData;
    }

    /**
     * Generates a Tile for the given Mission.
     */
    private Tile buildTile(Category category, Mission mission) {
      return new Tile(this, category, mission);
    }
  }

  /**
   * Handles rendering for a single Mission.
   */
  private static class Tile {
    private TileBuilder builder;
    private int width;
    private Color color;
    private List<String> contacts;
    private List<Integer> contactWidths;
    private int stepIndex;

    /**
     * Creates a new Tile for the indicated Mission.
     */
    private Tile(TileBuilder builder, Category category, Mission mission) {
      this.builder = builder;
      color = category.color;
      contacts = mission.getOrderedContactList().stream()
          .map(contact -> {
            String name = contact.getNameString();
            return name != null ? name : builder.localeData.string("missions.unknown_contact");
          })
          .collect(Collectors.toList());
      contactWidths = contacts.stream()
          .map(contact -> builder.contactMetrics.stringWidth(contact))
          .collect(Collectors.toList());
      width = contactWidths.stream().mapToInt(contact -> contact).sum() +
          builder.arrowWidth * (contacts.size() - 1);
      width += builder.padding * 2;
      stepIndex = mission.getStepIndex();
    }

    /**
     * Renders the Tile at the indicated position.
     */
    private void render(Graphics2D g, int x, int y) {
      g.setColor(color);
      g.fillRect(x, y, width, builder.height);
      int count = contacts.size();
      int x0 = x + builder.padding;
      int cx = x0;
      int cy = y + builder.contactMetrics.getAscent();
      g.setFont(builder.contactFont);

      for (int i = 0; i < count; i++) {
        g.setColor(i < stepIndex ? DISABLED_COLOR : ENABLED_COLOR);
        g.drawString(contacts.get(i), cx, cy);
        cx += contactWidths.get(i);

        if (i + 1 < count) {
          g.setColor(DISABLED_COLOR);
          g.drawString(" > ", cx, cy);
          cx += builder.arrowWidth;
        }
      }
    }
  }

  @Override
  public void reset() {
    tracker.clear();
  }

  /**
   * Categories into which Missions are organized.
   */
  private enum Category {
    MISSIONS(Color.ORANGE),
    OFFERING_ENERGY(Color.YELLOW);

    private Color color;

    private Category(Color color) {
      this.color = color;
    }
  }
}
