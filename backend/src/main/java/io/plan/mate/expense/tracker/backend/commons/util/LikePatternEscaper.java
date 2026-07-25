package io.plan.mate.expense.tracker.backend.commons.util;

public final class LikePatternEscaper {

  private LikePatternEscaper() {}

  public static String escape(final String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
