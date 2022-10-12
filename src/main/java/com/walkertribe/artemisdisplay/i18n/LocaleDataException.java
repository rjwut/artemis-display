package com.walkertribe.artemisdisplay.i18n;

/**
 * Exceptions thrown when reading locale data.
 */
class LocaleDataException extends RuntimeException {
  private static final long serialVersionUID = 6050749315080078350L;

  LocaleDataException(String message) {
    super(message);
  }

  LocaleDataException(Throwable cause) {
    super(cause);
  }
}
