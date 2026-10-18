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
  void ignoresWeightsOlderThanThirtyDays() {
    LocalDateTime now = LocalDateTime.of(2026, 10, 18, 12, 0);
    List<PastWeight> weights = List.of(
        new PastWeight(1, "", 81, now),
        new PastWeight(2, "", 80, now.minusDays(1)),
        new PastWeight(3, "", 100, now.minusDays(31)));
    assertEquals(WeightTrend.UP, WeightTrend.calculateLast30Days(weights, now));
    assertEquals(3, weights.size());
  }

  @Test
  void includesTheExactCutoffButNotEarlierEntries() {
    LocalDateTime now = LocalDateTime.of(2026, 10, 18, 12, 0);
    PastWeight current = new PastWeight(1, "", 81, now);
    PastWeight cutoff = new PastWeight(2, "", 80, now.minusDays(30));
    PastWeight older = new PastWeight(3, "", 80, now.minusDays(30).minusNanos(1));
    assertEquals(WeightTrend.UP, WeightTrend.calculateLast30Days(List.of(current, cutoff), now));
    assertEquals(WeightTrend.NONE, WeightTrend.calculateLast30Days(List.of(current, older), now));
  }

  @Test
  void ignoresFutureDates() {
    LocalDateTime now = LocalDateTime.of(2026, 10, 18, 12, 0);
    List<PastWeight> weights = List.of(
        new PastWeight(1, "", 100, now.plusNanos(1)),
        new PastWeight(2, "", 79, now),
        new PastWeight(3, "", 80, now.minusDays(1)));
    assertEquals(WeightTrend.DOWN, WeightTrend.calculateLast30Days(weights, now));
  }

  @Test
  void oldHistoryHasNoTrend() {
    LocalDateTime now = LocalDateTime.of(2026, 10, 18, 12, 0);
    assertEquals(WeightTrend.NONE, WeightTrend.calculateLast30Days(List.of(
        new PastWeight(1, "", 81, now.minusDays(31)),
        new PastWeight(2, "", 80, now.minusDays(32))), now));
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
