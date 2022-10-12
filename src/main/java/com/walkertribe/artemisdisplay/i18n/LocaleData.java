package com.walkertribe.artemisdisplay.i18n;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * Stores data for a single locale. Locales can be read from any of the following locations:
 * </p>
 * <ul>
 * <li>A <code>locale_{key}</code> directory in the Artemis Display resources</li>
 * <li>A <code>locale_{key}</code> directory in the working directory</li>
 * </ul>
 * <p>
 * The specifics of reading from each of these locations are handled by subclasses.
 * </p>
 * <ul>
 * <li>String data is provided in a "strings.txt" file. Unlike ResourceBundle, which uses
 * ISO-8859-1 encoding (Ick!), LocaleData reads strings in UTF-8. These files also support comments
 * by prefixing the line with a number sign (#).</li>
 * <li>If a custom font is needed, a .ttf file can be included. If more than one .ttf file is found,
 * the first one (ASCIIbetically) is used. Note that the timer display uses a separate, fixed-width
 * font which cannot be customized at this time.</li>
 * <li>All other files are ignored.</li>
 * </ul>
 * <p>
 * Locales are identified by a language tag string ("en-us", "de", "pt-br", etc.). Locale data
 * directories or .zip files must be named so as to identify their language tag:
 * </p>
 * <ul>
 * <li>Directory: locale_{tag}</li>
 * <li>.zip file: locale_{tag}.zip</li>
 * </ul>
 * <p>
 * Names are case-insensitive. The language tag should be as specific as possible (e.g. "es-mx", not
 * just "es").
 * </p>
 * @author rjwut
 */
public abstract class LocaleData {
  public static final float BASE_FONT_SIZE = 100;

  static final String PREFIX = "locale_";
  static final String STRINGS_FILE_NAME = "strings.txt";
  static final String FONT_EXTENSION = ".ttf";

  private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("en-us");
  private static final String DEFAULT_FONT_RESOURCE = "conthrax-sb.ttf";

  private static Map<Locale, LocaleData> supportedLocales = null;
  private static LocaleData current = null;
  private static Font defaultFont = null;

  /**
   * Returns the currently selected LocaleData. If none has been selected, it attempts to
   * auto-select one based on device settings, falling back to English (United States) if that
   * locale is not supported.
   */
  public static synchronized LocaleData get() {
    if (current == null) {
      set(Locale.getDefault().toLanguageTag());
    }

    return current;
  }

  /**
   * Sets the currently selected LocaleData based on the given language tag. This will be matched
   * against the supported locales, falling back to English (United States) if no match is found.
   * Returns the actual selected LocaleData instance.
   */
  public static synchronized LocaleData set(String tag) {
    List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(tag);
    List<Locale> compatibleLocales = Locale.filter(ranges, getSupportedLocales().keySet());
    Locale locale;

    if (compatibleLocales.isEmpty()) {
      locale = DEFAULT_LOCALE;
    } else {
      locale = compatibleLocales.get(0);
    }

    current = supportedLocales.get(locale);
    return current;
  }

  /**
   * Returns a Map of LocaleData objects keyed by their corresponding Locales.
   */
  public static synchronized Map<Locale, LocaleData> getSupportedLocales() {
    if (supportedLocales == null) {
      // Locate locale data in classpath and file system
      synchronized (LocaleData.class) {
        supportedLocales = new LinkedHashMap<>();
        ClasspathLocaleData.extractAll().forEach(LocaleData::add);
        FileSystemLocaleData.extractAll().forEach(LocaleData::add);
      }
    }

    return new HashMap<>(supportedLocales);
  }

  /**
   * Writes a <code>strings.txt</code> file containing the English localized strings.
   */
  public static void exportStrings() throws IOException {
    Path file = Paths.get(System.getProperty("user.dir"), STRINGS_FILE_NAME);
    String resource = PREFIX + DEFAULT_LOCALE.toLanguageTag().toLowerCase() + "/" + STRINGS_FILE_NAME;

    try (
      InputStream in = getResourceStream(resource);
    ) {
      Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Returns an InputStream to the named classpath resource.
   */
  static InputStream getResourceStream(String name) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
  }

  /**
   * Adds the given LocaleData object to the Map of supported Locales.
   */
  private static void add(LocaleData localeData) {
    supportedLocales.put(localeData.getLocale(), localeData);
  }

  /**
   * Returns the default Font. This should be used if the locale doesn't have its own Font.
   */
  private static Font getDefaultFont() {
    if (defaultFont == null) {
      synchronized (LocaleData.class) {
        defaultFont = loadFontInternal(getResourceStream(DEFAULT_FONT_RESOURCE));
      }
    }

    return defaultFont;
  }

  /**
   * Loads the font from the given InputStream.
   */
  private static Font loadFontInternal(InputStream in) {
    try {
      return Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(BASE_FONT_SIZE);
    } catch (FontFormatException | IOException ex) {
      throw new LocaleDataException(ex);
    }
  }

  private Locale locale;
  protected Map<String, String> strings;
  protected Font font;

  /**
   * Loads the data for this LocaleData object.
   */
  protected abstract void load();

  /**
   * Creates a new LocaleData for this tag.
   */
  public LocaleData(String tag) {
    this.locale = Locale.forLanguageTag(tag);
  }

  /**
   * Returns the Locale represented by this object.
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Returns the string with the given key. If arguments are provided, the placeholders in the
   * string will be replaced with those arguments, as in MessageFormat.format().
   */
  public String string(String key, Object... args) {
    assertLoaded();
    String str = strings.get(key);

    if (str == null) {
      return locale.toLanguageTag() + ":" + key;
    }

    str = str.replaceAll("\\\\n", "\n");
    return args.length > 0 ? MessageFormat.format(str, args) : str;
  }

  /**
   * Convenience method for calling string() and appending the name of an enum value to a key. No
   * arguments are supported for this method.
   */
  public String string(String key, Enum<?> value) {
    return string(key + "." + value.name().toLowerCase());
  }

  /**
   * Returns the given value formatted as a percent, as appropriate for this locale.
   */
  public String formatPercent(float perc) {
    return NumberFormat.getPercentInstance(locale).format(perc);
  }

  /**
   * Returns the Font for this LocaleData. The default Font will be returned if this locale doesn't
   * override it.
   */
  public Font getFont() {
    assertLoaded();
    return font;
  }

  /**
   * Returns the Font for this LocaleData, set to the given size. The default Font will be returned
   * if this locale doesn't override it.
   */
  public Font getFont(float size) {
    assertLoaded();
    return font.deriveFont(size);
  }

  /**
   * Reads the string data from the given InputStream.
   */
  protected void loadStrings(InputStream in) {
    try (
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    ) {
      strings = new HashMap<>();
      String line;

      while ((line = reader.readLine()) != null) {
        line = line.trim();

        // Skip blank lines and comments
        if (line.length() == 0 || line.startsWith("#")) {
          continue;
        }

        // Split line on the first equals sign
        int eqPos = line.indexOf('=');

        if (eqPos == -1) {
          throw new RuntimeException("Line must contain an equals sign: " + line);
        }

        if (eqPos == 0) {
          throw new RuntimeException("Line cannot start with an equals sign: " + line);
        }

        strings.put(line.substring(0, eqPos), line.substring(eqPos + 1));
      }
    } catch (IOException ex) {
      throw new LocaleDataException(ex);
    }
  }

  /**
   * Loads the Font for this locale from the given InputStream.
   */
  protected void loadFont(InputStream in) {
    font = loadFontInternal(in);
  }

  /**
   * Ensures that the data for this LocaleData is loaded.
   */
  private synchronized void assertLoaded() {
    if (strings != null) {
      return;
    }

    load();

    if (font == null) {
      font = getDefaultFont();
    }
  }
}
