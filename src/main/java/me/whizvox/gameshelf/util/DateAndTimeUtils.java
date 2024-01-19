package me.whizvox.gameshelf.util;

import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class DateAndTimeUtils {

  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  public static String formatDateTime(TemporalAccessor temporal) {
    return DATE_TIME_FORMATTER.format(temporal);
  }

  public static String formatDate(TemporalAccessor temporal) {
    return DATE_FORMATTER.format(temporal);
  }

  public static LocalDateTime parseDateTime(String str) {
    return LocalDateTime.from(DATE_TIME_FORMATTER.parse(str));
  }

  public static LocalDate parseDate(String str) {
    return LocalDate.from(DATE_FORMATTER.parse(str));
  }

  // mongodb doesn't store nanoseconds
  public static boolean equalsMinusNanos(@Nullable LocalDateTime a, @Nullable LocalDateTime b) {
    if (a == null) {
      return b == null;
    } else if (b == null) {
      return false;
    }
    return a.minusNanos(a.getNano()).equals(b.minusNanos(b.getNano()));
  }

}
