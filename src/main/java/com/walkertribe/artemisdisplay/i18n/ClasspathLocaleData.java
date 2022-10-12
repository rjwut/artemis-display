package com.walkertribe.artemisdisplay.i18n;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * LocaleData implementation read from the classpath.
 * @author rjwut
 */
class ClasspathLocaleData extends LocaleData {
  private static final String[] LOCALES = { "en-us", "es-mx" };

  /**
   * Create ClasspathLocaleData objects for each locale discovered on the classpath.
   */
  static Stream<ClasspathLocaleData> extractAll() {
    return Arrays.stream(LOCALES).map(ClasspathLocaleData::new);
  }

  private String resource;

  ClasspathLocaleData(String tag) {
    super(tag);
    resource = PREFIX + tag + '/' + STRINGS_FILE_NAME;
  }

  @Override
  protected void load() {
    loadStrings(getResourceStream(resource));
  }
}
