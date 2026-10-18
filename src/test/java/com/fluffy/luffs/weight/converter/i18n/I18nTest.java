package com.fluffy.luffs.weight.converter.i18n;

import com.fluffy.luffs.weight.converter.controllers.model.Weight;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class I18nTest {
  @ParameterizedTest
  @ValueSource(strings = {"en", "fr", "de", "es"})
  void allBundlesContainTheSameNonEmptyKeys(String language) throws Exception {
    Properties fallback = properties("Messages.properties");
    Properties translated = properties("Messages_" + language + ".properties");
    assertEquals(fallback.stringPropertyNames(), translated.stringPropertyNames());
    for (String key : fallback.stringPropertyNames()) {
      assertFalse(translated.getProperty(key).isBlank(), key);
    }
  }

  @ParameterizedTest
  @MethodSource("translations")
  void selectsTranslationsAndSubstitutesPrompts(Locale locale, String unit, String prompt) {
    assertEquals(unit, I18n.text(locale, "unit.kilograms"));
    assertEquals(prompt, I18n.text(locale, "input.prompt", unit));
    assertFalse(I18n.text(locale, "history.empty").contains("\\n"));
    assertTrue(I18n.text(locale, "history.empty").contains("\n\n"));
  }

  static Stream<Arguments> translations() {
    return Stream.of(
        Arguments.of(Locale.UK, "Kilograms", "Enter the weight in Kilograms"),
        Arguments.of(Locale.FRANCE, "Kilogrammes", "Saisissez le poids en Kilogrammes"),
        Arguments.of(Locale.GERMANY, "Kilogramm", "Gewicht in Kilogramm eingeben"),
        Arguments.of(Locale.forLanguageTag("es-ES"), "Kilogramos", "Introduzca el peso en Kilogramos"));
  }

  @Test
  void unsupportedLanguagesFallBackToEnglish() {
    assertEquals("Save", I18n.text(Locale.JAPAN, "action.save"));
    assertEquals("Save", I18n.text(Locale.ENGLISH, "action.save"));
    assertEquals("Enregistrer", I18n.text(Locale.CANADA_FRENCH, "action.save"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"fr-FR", "de-DE", "es-ES"})
  void decimalCommaInputProducesCanonicalNumericValues(String language) {
    Locale locale = Locale.forLanguageTag(language);
    assertEquals(81.65, Weight.KG.getKilos(I18n.normalizeNumber(locale, " 81,65 ")), 1e-9);
    assertEquals(81.6466266, Weight.LBS.getKilos(I18n.normalizeNumber(locale, "180,0")), 1e-9);
    assertEquals("81.65", I18n.normalizeNumber(locale, "81.65"));
    assertEquals("81,65", I18n.displayNumber(locale, "81.65"));
    assertThrows(NumberFormatException.class,
        () -> Double.parseDouble(I18n.normalizeNumber(locale, "1.234,5")));
  }

  @ParameterizedTest
  @ValueSource(strings = {"en", "fr", "de", "es"})
  void displayFormattingDoesNotChangeCanonicalStoredWeights(String language) {
    Locale locale = Locale.forLanguageTag(language);
    String canonical = "10st 2lb 8oz";
    String display = I18n.displayWeight(locale, canonical);
    assertTrue(display.contains("10"));
    assertTrue(display.contains("2"));
    assertTrue(display.contains("8"));
    assertEquals(display, I18n.displayWeight(locale, "10st 2lbs 8oz"));
    assertEquals("10st 2lb 8oz", canonical);
    assertEquals("legacy custom text", I18n.displayWeight(locale, "legacy custom text"));
  }

  @Test
  void datesUseTheCalendarYearAndLocale() {
    LocalDateTime date = LocalDateTime.of(2020, 12, 31, 23, 30);
    assertEquals("31 Dec 2020", I18n.displayDate(Locale.UK, date));
    assertEquals("31.12.2020", I18n.displayDate(Locale.GERMANY, date));
    assertNotEquals(I18n.displayDate(Locale.UK, date), I18n.displayDate(Locale.FRANCE, date));
  }

  private Properties properties(String filename) throws Exception {
    Properties properties = new Properties();
    try (var stream = getClass().getResourceAsStream("/i18n/" + filename)) {
      assertNotNull(stream, filename);
      properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
    return properties;
  }
}
