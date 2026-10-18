package com.fluffy.luffs.weight.converter.controllers.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WeightTest {
  @Test
  void kilogramsAreStoredWithoutConversion() {
    assertEquals(81.6466266, Weight.KG.getKilos("81.6466266"), 1e-9);
  }

  @Test
  void poundsAreConvertedToKilogramsWithoutDisplayRounding() {
    assertEquals(81.6466266, Weight.LBS.getKilos("180"), 1e-9);
    assertEquals(0.45359237, Weight.LBS.getKilos("1"), 1e-9);
  }
}
