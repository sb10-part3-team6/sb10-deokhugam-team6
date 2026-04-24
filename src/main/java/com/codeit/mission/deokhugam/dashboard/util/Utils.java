package com.codeit.mission.deokhugam.dashboard.util;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Utils {
  public static List<LocalDateTime> calculatePeriod(PeriodType periodType, LocalDateTime aggregatedAt){
    return List.of(periodType.calculateStart(aggregatedAt), periodType.calculateEnd(aggregatedAt));
  }
}
