package com.codeit.mission.deokhugam.dashboard.util;

import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidJobParameterException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class JobParameterUtils {

  private JobParameterUtils() {
  }

  public record ParameterValue(String name, String value) {

  }

  public static ParameterValue parameter(String name, String value) {
    return new ParameterValue(name, value);
  }

  public static void validateRequired(ParameterValue... parameters) {
    Map<String, Object> details = new LinkedHashMap<>();

    for (ParameterValue parameter : parameters) {
      if (parameter.value() == null || parameter.value().isBlank()) {
        details.put(parameter.name(), normalize(parameter.value()));
      }
    }

    if (!details.isEmpty()) {
      throw new InvalidJobParameterException(details);
    }
  }

  public static UUID parseUuid(String name, String rawValue) {
    String value = requireText(name, rawValue);

    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      throw invalid(name, value);
    }
  }

  public static Instant parseLocalDateTime(String name, String rawValue) {
    String value = requireText(name, rawValue);

    try {
      return Instant.parse(value);
    } catch (DateTimeParseException e) {
      throw invalid(name, value);
    }
  }

  public static <E extends Enum<E>> E parseEnum(String name, String rawValue, Class<E> enumType) {
    String value = requireText(name, rawValue);

    try {
      return Enum.valueOf(enumType, value);
    } catch (IllegalArgumentException e) {
      throw invalid(name, value);
    }
  }

  private static String requireText(String name, String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      throw invalid(name, rawValue);
    }

    return rawValue;
  }

  private static InvalidJobParameterException invalid(String name, String rawValue) {
    return new InvalidJobParameterException(Map.of(name, normalize(rawValue)));
  }

  private static String normalize(String rawValue) {
    return rawValue != null ? rawValue : "null";
  }
}
