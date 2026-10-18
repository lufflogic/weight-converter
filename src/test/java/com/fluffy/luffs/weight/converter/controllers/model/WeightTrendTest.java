package com.fluffy.luffs.weight.converter.controllers.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WeightTrendTest {
  @ParameterizedTest
  @MethodSource("histories")
  void comparesConsecutiveRollingAverages(WeightTrend expected, double[] kilos) {
    assertEquals(expected, WeightTrend.calculate(history(kilos)));
  }

  static Stream<Arguments> histories() {
    return Stream.of(
        Arguments.of(WeightTrend.NONE, new double[] {}),
        Arguments.of(WeightTrend.NONE, new double[] {80}),
        Arguments.of(WeightTrend.NONE, new double[] {80, 80}),
        Arguments.of(WeightTrend.UP, new double[] {81, 80}),
        Arguments.of(WeightTrend.DOWN, new double[] {79, 80}),
        Arguments.of(WeightTrend.UP, new double[] {82, 80, 81}),
        Arguments.of(WeightTrend.DOWN, new double[] {80, 70, 81}),
        Arguments.of(WeightTrend.NONE, new double[] {80, 70, 90, 75, 85, 80}),
        Arguments.of(WeightTrend.UP, new double[] {81, 80, 79, 78, 77, 76, 200}),
        Arguments.of(WeightTrend.DOWN, new double[] {76, 77, 78, 79, 80, 81, 0}),
        Arguments.of(WeightTrend.NONE, new double[] {80, Double.NaN}),
        Arguments.of(WeightTrend.NONE, new double[] {80, Double.POSITIVE_INFINITY}));
  }

  @Test
  void recalculatesWhenHistoryChanges() {
    List<PastWeight> history = new ArrayList<>(history(81, 79, 80));
    assertEquals(WeightTrend.UP, WeightTrend.calculate(history));
    history.remove(0);
    assertEquals(WeightTrend.DOWN, WeightTrend.calculate(history));
    history.remove(0);
    assertEquals(WeightTrend.NONE, WeightTrend.calculate(history));
    history.clear();
    assertEquals(WeightTrend.NONE, WeightTrend.calculate(history));
  }

  @Test
  void equivalentWeightsEnteredInDifferentUnitsHaveNoTrend() {
    assertEquals(WeightTrend.NONE,
        WeightTrend.calculate(history(Weight.LBS.getKilos("180"), Weight.KG.getKilos("81.6466266"))));
  }

  private static List<PastWeight> history(double... kilos) {
    LocalDateTime now = LocalDateTime.of(2026, 10, 18, 12, 0);
    return IntStream.range(0, kilos.length)
        .mapToObj(index -> new PastWeight(index, "", kilos[index], now.minusDays(index)))
        .toList();
  }
}
