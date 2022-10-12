package com.walkertribe.artemisdisplay.display.layout;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.walkertribe.artemisdisplay.ArtemisDisplay;
import com.walkertribe.artemisdisplay.display.Display;
import com.walkertribe.ian.Context;

/**
 * Responsible for parsing layout files and returning the result as a Display implementation.
 * @author rjwut
 */
public class LayoutParser {
  private ArtemisDisplay app;
  private Context ctx;

  public LayoutParser(ArtemisDisplay app, Context ctx) {
    this.app = app;
    this.ctx = ctx;
  }

  /**
   * Parses the given layout File and returns the constructed Display object.
   */
  public Display parse(File file) throws IOException {
    try (
        FileInputStream in = new FileInputStream(file);
    ) {
      JSONObject root = new JSONObject(new JSONTokener(in));
      return build(root);
    }
  }

  /**
   * Returns a Display object as defined by the given JSONObject. Applied recursively as needed.
   */
  Display build(JSONObject config) {
    String typeStr = config.getString("type");
    String enumName = convertTypeName(typeStr);
    Display display = buildDisplay(enumName);

    if (display == null) {
      display = buildLayout(enumName, config);
    }

    if (display == null) {
      throw new IllegalArgumentException("Unknown display type: " + typeStr);
    }

    if (config.has("border")) {
      display.setBorder(config.getBoolean("border"));
    }

    if (config.has("title")) {
      display.setTitle(config.getString("title"));
    }

    return display;
  }

  /**
   * Returns a new instance of the named Display object, or null if the given name does not
   * correspond to a Display.Type.
   */
  private Display buildDisplay(String enumName) {
    try {
      return Display.Type.valueOf(enumName).newInstance(app, ctx);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  /**
   * Returns a new Layout instance according to the given Layout.Type name and JSON configuration;
   * or null if the name does not correspond to a Layout.Type.
   */
  private Layout<?> buildLayout(String enumName, JSONObject layoutConfig) {
    try {
      return Layout.Type.valueOf(enumName).newInstance(app, ctx, this, layoutConfig);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  /**
   * Converts the given String to uppercase and changes any hyphens (-) to underscores (_). This is
   * used to convert Display or Layout type names used in JSON files to their corresponding enum
   * names. 
   */
  private static String convertTypeName(String typeName) {
    return Arrays.stream(typeName.split("\\-"))
        .map(part -> part.toUpperCase())
        .collect(Collectors.joining("_"));
  }
}
