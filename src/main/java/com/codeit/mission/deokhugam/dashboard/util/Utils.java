package com.codeit.mission.deokhugam.dashboard.util;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.dto.ParsedCursors;
import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidCursorValueException;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.List;

public class Utils {
  public static List<Instant> calculatePeriod(PeriodType periodType, Instant aggregatedAt){
    return List.of(periodType.calculateStart(aggregatedAt), periodType.calculateEnd(aggregatedAt));
  }

  public static ParsedCursors parseCursors(String cursor, String after){
    try{
      if(cursor == null){
        return new ParsedCursors(null, null);
      }
      return new ParsedCursors(Long.parseLong(cursor), Instant.parse(after));
    } catch (NumberFormatException | DateTimeException e){
      throw new InvalidCursorValueException();
    }
  }
}
