package com.fluffy.luffs.weight.converter.controllers.model;

import java.util.List;

/** Change between consecutive rolling averages of up to five weights. */
public enum WeightTrend {
  UP,
  DOWN,
  NONE;

  /** The history must be ordered newest first, with weights expressed in kilograms. */
  public static WeightTrend calculate(List<PastWeight> history) {
    if (history.size() < 2) {
      return NONE;
    }

    int windowSize = Math.min(5, history.size() - 1);
    double current = 0;
    double previous = 0;
    for (int index = 0; index < windowSize; index++) {
      current += history.get(index).getKilos() / windowSize;
      previous += history.get(index + 1).getKilos() / windowSize;
    }
    double change = current - previous;
    if (!Double.isFinite(change) || Math.abs(change) < 1e-9) {
      return NONE;
    }
    return change > 0 ? UP : DOWN;
  }
}
