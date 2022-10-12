package com.walkertribe.artemisdisplay;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import org.json.JSONException;

import com.walkertribe.artemisdisplay.display.Display;
import com.walkertribe.artemisdisplay.display.layout.LayoutParser;
import com.walkertribe.artemisdisplay.i18n.LocaleData;
import com.walkertribe.ian.Context;
import com.walkertribe.ian.DefaultContext;
import com.walkertribe.ian.FilePathResolver;
import com.walkertribe.ian.world.Artemis;

/**
 * Stores configuration options for ArtemisDisplay.
 * @author rjwut
 */
public class Configuration {
  /**
   * The action to perform.
   */
  public static enum Action {
    RUN,            // run the display
    EXPORT_STRINGS, // export the locale strings file
    HELP,           // display command line help
  }

  /**
   * The available render options. These typically trade performance for prettiness.
   */
  public static enum RenderOption {
    ANTIALIASING,
    SUBPIXEL_FONT_RENDERING,
    DRAW_SILHOUETTES,
    IMPACT_SHAKE,
    IMPACT_DIM,
    IMPACT_STATIC
  }

  public static void printUsage() {
    System.out.println("Usage:");
    System.out.println();
    System.out.println("java [Java options] -jar <jarFile> [ArtemisDisplay options]");
    System.out.println();
    FLAGS.values().forEach(System.out::println);
  }

  private static final String[] ARTEMIS_INSTALL_DIRECTORIES = {
    "C:\\Program Files (x86)\\Artemis",
    "C:\\games\\Artemis" // Where Thom recommends installing in his FAQ
  };

  private static final Map<String, Flag> FLAGS = new TreeMap<>();

  /**
   * Adds a new Flag to the FLAGS Map.
   */
  private static void addFlag(String name, String argName, String description,
      BiConsumer<Configuration, List<String>> applyFn) {
    FLAGS.put(name, new Flag(name, argName, description, applyFn));
  }

  static {
    addFlag("antialias", null, "Turn on antialiasing", (config, args) -> {
      config.setRenderOption(RenderOption.ANTIALIASING, true);
    });
    addFlag("artemis", "directory", "Set Artemis install directory", (config, args) -> {
      config.setArtemisInstallPath(new File(args.get(0)));
    });
    addFlag("dim", null, "Enable display dimming on impact", (config, args) -> {
      config.setRenderOption(RenderOption.IMPACT_DIM, true);
    });
    addFlag("display", "type", "The display type to show (if not using --layout)", (config, args) -> {
      String enumName = args.get(0).toUpperCase().replace('-', '_');
      config.setDisplayType(Display.Type.valueOf(enumName));
    });
    addFlag("export-strings", null, "Export localization strings file", (config, args) -> {
      config.setAction(Action.EXPORT_STRINGS);
    });
    addFlag("force-dialog", null, "Always show configuration dialog", (config, args) -> {
      config.setForceDialog(true);
    });
    addFlag("help", null, "Print this message", (config, args) -> {
      config.setAction(Action.HELP);
    });
    addFlag("host", "nameOrAddress", "Address or host name of Artemis server", (config, args) -> {
      config.setHost(args.get(0));
    });
    addFlag("layout", "jsonFile", "The layout file to use (if not using --display)", (config, args) -> {
      config.setLayoutFile(new File(args.get(0)));
    });
    addFlag("locale", "tag", "Display locale", (config, args) -> {
      config.setLocale(args.get(0));
    });
    addFlag("mode", "mode", "Window mode (fullscreen, windowed-fullscreen, windowed)", (config, args) -> {
      String enumName = args.get(0).toUpperCase().replace('-', '_');
      config.setWindowMode(WindowMode.valueOf(enumName));
    });
    addFlag("monitor", "number", "Which monitor to show the display on", (config, args) -> {
      config.setMonitor(Integer.parseInt(args.get(0)));
    });
    addFlag("no-shake", null, "Disable display shake on impact", (config, args) -> {
      config.setRenderOption(RenderOption.IMPACT_SHAKE, false);
    });
    addFlag("no-silhouettes", null, "Always render arrows instead of ship silhouettes", (config, args) -> {
      config.setRenderOption(RenderOption.DRAW_SILHOUETTES, false);
    });
    addFlag("ship", "index", "Ship index to display (0 - 7 inclusive)", (config, args) -> {
      config.setShipIndex((byte) (Byte.parseByte(args.get(0)) - 1));
    });
    addFlag("static", null, "Fill display with static on impact", (config, args) -> {
      config.setRenderOption(RenderOption.IMPACT_STATIC, true);
    });
    addFlag("subpixel-font", null, "Enable subpixel font rendering", (config, args) -> {
      config.setRenderOption(RenderOption.SUBPIXEL_FONT_RENDERING, true);
    });
  }

  private Action action = Action.RUN;
  private String host;
  private byte shipIndex = 0;
  private File artemisInstallPath = findArtemis();
  private WindowMode windowMode = WindowMode.FULLSCREEN;
  private int monitor = 1;
  private Display.Type displayType = Display.Type.ALERT;
  private File layoutFile;
  private Set<RenderOption> renderOptions = new HashSet<>();
  private boolean forceDialog;
  private Context ctx;

  /**
   * Creates a new Configuration object. The DRAW_SILHOUETTES and IMPACT_SHAKE options are turned on
   * by default.
   */
  public Configuration(String[] args) {
    renderOptions.add(RenderOption.DRAW_SILHOUETTES);
    renderOptions.add(RenderOption.IMPACT_SHAKE);
    Flag flagObj = null;
    List<String> switchArgs = null;

    for (String arg : args) {
      if (arg.startsWith("--")) {
        if (flagObj != null) {
          flagObj.apply(this, switchArgs);
        }

        arg = arg.substring(2);
        flagObj = FLAGS.get(arg);

        if (flagObj == null) {
          throw new IllegalArgumentException("Unknown switch: --" + arg);
        }

        switchArgs = new ArrayList<>();
      } else {
        if (flagObj == null) {
          throw new IllegalArgumentException("Expected a switch; got " + arg);
        }

        switchArgs.add(arg);
      }
    }

    if (flagObj != null) {
      flagObj.apply(this, switchArgs);
    }
  }

  /**
   * The action to be performed.
   */
  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  /**
   * The host to which ArtemisDisplay should connect.
   */
  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    host = host != null ? host.trim() : "";
    this.host = host.length() > 0 ? host : null;
  }

  /**
   * The index of the ship to display.
   */
  public byte getShipIndex() {
    return shipIndex;
  }

  public void setShipIndex(byte shipIndex) {
    if (shipIndex < 0 || shipIndex >= Artemis.SHIP_COUNT) {
      throw new IllegalArgumentException("Invalid ship index: " + shipIndex);
    }

    this.shipIndex = shipIndex;
  }

  /**
   * The path to the Artemis installation directory.
   */
  public File getArtemisInstallPath() {
    return artemisInstallPath;
  }

  public void setArtemisInstallPath(File artemisInstallPath) {
    if (artemisInstallPath != null) {
      validateArtemisInstallPath(artemisInstallPath);
    }

    this.artemisInstallPath = artemisInstallPath;
  }

  /**
   * The window mode to use when showing the display.
   */
  public WindowMode getWindowMode() {
    return windowMode;
  }

  public void setWindowMode(WindowMode windowMode) {
    if (windowMode == null) {
      throw new IllegalArgumentException("Display mode cannot be null");
    }

    this.windowMode = windowMode;
  }

  /**
   * Which monitor to show the display on.
   */
  public int getMonitor() {
    return monitor;
  }

  public void setMonitor(int monitor) {
    if (monitor < 1) {
      throw new IllegalArgumentException("Monitor must not be less than 1");
    }

    int deviceCount = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;

    if (monitor > deviceCount) {
      throw new IllegalArgumentException("There is no monitor " + monitor);
    }

    this.monitor = monitor;
  }

  /**
   * The type of Display to show. If you're using a layout, this will be null.
   */
  public Display.Type getDisplayType() {
    return displayType;
  }

  public void setDisplayType(Display.Type displayType) {
    this.displayType = displayType;

    if (displayType != null) {
      layoutFile = null;
    }
  }

  /**
   * The path to the layout file. If you're not using a layout, this will be null.
   */
  public File getLayoutFile() {
    return layoutFile;
  }

  public void setLayoutFile(File layoutFile) {
    if (layoutFile != null) {
      validateLayoutFile(layoutFile);
      this.layoutFile = layoutFile;
      displayType = null;
    }

    this.layoutFile = layoutFile;
  }

  /**
   * Returns whether the given RenderOption is enabled.
   */
  public boolean getRenderOption(RenderOption option) {
    return renderOptions.contains(option);
  }

  public void setRenderOption(RenderOption option, boolean on) {
    if (on) {
      renderOptions.add(option);
    } else {
      renderOptions.remove(option);
    }
  }

  /**
   * Sets the Locale. If the given Locale is not supported, it will attempt to fall back to another
   * Locale in the same language, or English if no Locale in the same language is supported.
   */
  public void setLocale(Locale locale) {
    LocaleData.set(locale.toLanguageTag());
  }

  /**
   * Sets the Locale by its language tag. If the given Locale is not supported, it will attempt to
   * fall back to another Locale in the same language, or English if no Locale in the same language
   * is supported.
   */
  public void setLocale(String tag) {
    LocaleData.set(tag);
  }

  /**
   * Returns true if the configuration dialog should be shown even if enough information was
   * provided via the command line to launch the display immediately.
   */
  public boolean isForceDialog() {
    return forceDialog;
  }

  public void setForceDialog(boolean forceDialog) {
    this.forceDialog = forceDialog;
  }

  /**
   * Builds the Display object defined by this object.
   */
  public Display buildDisplay(ArtemisDisplay app) {
    Context ctx = getContext();
    Display display;

    if (displayType != null) {
      display = displayType.newInstance(app, ctx);
    } else {
      try {
        display = new LayoutParser(app, ctx).parse(layoutFile);
      } catch (JSONException ex) {
        // TODO Make layout parsing throw more helpful messages
        throw new IllegalArgumentException("Not a valid layout file: " + layoutFile + "\n" + ex.getMessage());
      } catch (IOException ex) {
        throw new IllegalArgumentException(ex);
      }

    }

    return display;
  }

  /**
   * Throws an IllegalArgumentException if the given path is not a usable Artemis install directory.
   * It validates if the given path is an existing readable directory, which contains a readable
   * directory named "dat", which contains a readable file named "vesselData.xml".
   */
  private static void validateArtemisInstallPath(File path) {
    if (!path.exists()) {
      throw new IllegalArgumentException("Path does not exist: " + path);
    }

    if (!path.isDirectory()) {
      throw new IllegalArgumentException("Not a directory: " + path);
    }

    if (!path.canRead()) {
      throw new IllegalArgumentException("Cannot read directory: " + path);
    }

    File datDir = new File(path, "dat");

    if (!datDir.exists()) {
      throw new IllegalArgumentException("Install directory doesn't contain /dat: " + path);
    }

    if (!datDir.isDirectory()) {
      throw new IllegalArgumentException("Not a directory: " + datDir);
    }

    if (!datDir.canRead()) {
      throw new IllegalArgumentException("Cannot read directory: " + datDir);
    }

    File vesselDataFile = new File(datDir, "vesselData.xml");

    if (!vesselDataFile.exists()) {
      throw new IllegalArgumentException("File not found: " + vesselDataFile);
    }

    if (!vesselDataFile.isFile()) {
      throw new IllegalArgumentException("Not a file: " + vesselDataFile);
    }

    if (!vesselDataFile.canRead()) {
      throw new IllegalArgumentException("Cannot read file: " + vesselDataFile);
    }
  }

  /**
   * Validates that the given path is a readable .json file.
   */
  private static void validateLayoutFile(File file) {
    if (!file.exists()) {
      throw new IllegalArgumentException("File does not exist: " + file);
    }

    if (!file.isFile()) {
      throw new IllegalArgumentException("Not a file: " + file);
    }

    if (!file.canRead()) {
      throw new IllegalArgumentException("Can't read file: " + file);
    }

    if (!file.getName().toLowerCase().endsWith(".json")) {
      throw new IllegalArgumentException("Not a .json file: " + file);
    }
  }

  /**
   * Returns true if we have enough information to launch the display.
   */
  public boolean isReady() {
    return host != null && (displayType != null || layoutFile != null);
  }

  /**
   * Returns a Context object for the specified Artemis install directory, or null if none is
   * specified.
   */
  public Context getContext() {
    if (ctx == null) {
      if (artemisInstallPath == null) {
        return null;
      }

      ctx = new DefaultContext(new FilePathResolver(artemisInstallPath));
    }

    return ctx;
  }

  /**
   * Attempts to locate the Artemis installation directory. If the directory is located, a File
   * object for that directory will be returned; otherwise, this method will return null. Note: This
   * will only be attempted on Windows systems; on other operating systems, this method always
   * returns null.
   */
  private static File findArtemis() {
    if (!System.getProperty("os.name").startsWith("Windows")) {
      return null;
    }

    File dir = null;

    for (String path : ARTEMIS_INSTALL_DIRECTORIES) {
      dir = new File(path);

      try {
        validateArtemisInstallPath(dir);
        break;
      } catch (IllegalArgumentException ex) {
        dir = null;
      }
    }

    return dir;
  }

  /**
   * Handles a command-line flag.
   */
  private static class Flag implements Comparable<Flag> {
    private String name;
    private String argName;
    private String description;
    private BiConsumer<Configuration, List<String>> applyFn;

    private Flag(String name, String argName, String description, BiConsumer<Configuration, List<String>> applyFn) {
      this.name = name;
      this.argName = argName;
      this.description = description;
      this.applyFn = applyFn;
    }

    /**
     * Applies this flag to the configuration.
     */
    private void apply(Configuration config, List<String> args) {
      int argCount = this.argName == null ? 0 : 1;

      if (args.size() != argCount) {
        throw new IllegalArgumentException(
            "Expected " + argCount + " argument(s) for --" + name + "; got " + args.size()
        );
      }

      applyFn.accept(config, args);
    }

    @Override
    public String toString() {
      return "--" + name + (argName != null ? " <" + argName + ">" : "") + "\n    " + description;
    }

    @Override
    public int compareTo(Flag o) {
      return name.compareTo(o.name);
    }
  }
}
