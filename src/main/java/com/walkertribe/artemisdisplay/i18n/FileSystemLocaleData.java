package com.walkertribe.artemisdisplay.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileSystemLocaleData extends LocaleData {
  private static final Predicate<File> IS_LOCALEDATA = file -> file.isDirectory() && file.getName().toLowerCase().startsWith(PREFIX);

  @SuppressWarnings("unchecked")
  public static <T extends FileSystemLocaleData> Stream<T> extractAll() {
    Path dir = Paths.get(System.getProperty("user.dir"));

    try {
      return (Stream<T>) Files.list(dir)
        .map(Path::toFile)
        .filter(IS_LOCALEDATA)
        .map(FileSystemLocaleData::new);
    } catch (IOException ex) {
      throw new LocaleDataException(ex);
    }
  }
  private File stringsFile;
  private File fontFile;

  FileSystemLocaleData(File directory) {
    super(extractTag(directory));

    for (File file : directory.listFiles()) {
      String filename = file.getName();

      if (STRINGS_FILE_NAME.equals(filename)) {
        stringsFile = file;
      } else if (fontFile == null && filename.endsWith(FONT_EXTENSION)) {
        fontFile = file;
      }

      if (stringsFile != null && fontFile != null) {
        break;
      }
    }

    if (stringsFile == null) {
      throw new LocaleDataException(
        "No " + STRINGS_FILE_NAME + " file found in locale directory: " + getLocale().getDisplayName()
      );
    }
  }

  @Override
  protected void load() {
    loadStrings(getInputStream(stringsFile));

    if (fontFile != null) {
      loadFont(getInputStream(fontFile));
    }
  }

  /**
   * Returns an InputStream for the given File.
   */
  private InputStream getInputStream(File file) {
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException ex) {
      throw new LocaleDataException(ex);
    }
  }

  /**
   * Returns the locale tag for the directory.
   */
  private static String extractTag(File directory) {
    return directory.getName().substring(PREFIX.length());
  }
}
