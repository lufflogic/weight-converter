package com.fluffy.luffs.weight.converter.i18n;

import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Locale-based UI text. Stored weights and numeric values retain their canonical format. */
public final class I18n {
  private static final String BUNDLE = "i18n.Messages";
  private static final Pattern WEIGHT = Pattern.compile("(-?\\d+)st\\s+(-?\\d+)lbs?\\s+(-?\\d+)oz");

  private I18n() {}

  public static Locale locale() {
    return supportedLocale(Locale.getDefault());
  }

  private static Locale supportedLocale(Locale locale) {
    return switch (locale.getLanguage()) {
      case "en", "fr", "de", "es" -> locale;
      default -> Locale.UK;
    };
  }

  public static String text(String key, Object... arguments) {
    return text(locale(), key, arguments);
  }

  public static String text(Locale locale, String key, Object... arguments) {
    Locale selected = supportedLocale(locale);
    String value = ResourceBundle.getBundle(BUNDLE, selected).getString(key);
    return arguments.length == 0 ? value : new MessageFormat(value, selected).format(arguments);
  }

  public static String displayWeight(String canonicalWeight) {
    return displayWeight(locale(), canonicalWeight);
  }

  public static String displayWeight(Locale locale, String canonicalWeight) {
    Matcher matcher = WEIGHT.matcher(canonicalWeight);
    return matcher.matches()
        ? text(locale, "weight.format", matcher.group(1), matcher.group(2), matcher.group(3))
        : canonicalWeight;
  }

  public static String normalizeNumber(String input) {
    return normalizeNumber(locale(), input);
  }

  public static String normalizeNumber(Locale locale, String input) {
    char separator = DecimalFormatSymbols.getInstance(supportedLocale(locale)).getDecimalSeparator();
    return input.strip().replace(separator, '.');
  }

  public static String displayNumber(String canonicalNumber) {
    return displayNumber(locale(), canonicalNumber);
  }

  public static String displayNumber(Locale locale, String canonicalNumber) {
    char separator = DecimalFormatSymbols.getInstance(supportedLocale(locale)).getDecimalSeparator();
    return canonicalNumber.replace('.', separator);
  }

  public static String displayDate(Locale locale, LocalDateTime date) {
    return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(supportedLocale(locale)));
  }
}
