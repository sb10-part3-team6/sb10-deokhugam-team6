package com.codeit.mission.deokhugam.dashboard;

import java.time.LocalDateTime;

public enum PeriodType {
  // 일간
  DAILY {
    @Override
    public LocalDateTime calculateStart(LocalDateTime aggregatedAt) {
      return aggregatedAt.minusDays(1); // 집계 날짜에서 하루를 뺀 날
    }
  },
  // 주간
  WEEKLY {
    @Override
    public LocalDateTime calculateStart(LocalDateTime aggregatedAt) {
      return aggregatedAt.minusWeeks(1); // 집계 날짜에서 한 주를 뺀 날짜
    }
  },
  // 월간
  MONTHLY {
    @Override
    public LocalDateTime calculateStart(LocalDateTime aggregatedAt) {
      return aggregatedAt.minusMonths(1); // 집계 날짜에서 한 달을 뺀 날짜
    }
  },
  // 상시
  ALL_TIME {
    @Override
    public LocalDateTime calculateStart(LocalDateTime aggregatedAt) {
      return LocalDateTime.of(1970,1,1,0,0);
    }
  };

  public abstract LocalDateTime calculateStart(LocalDateTime aggregatedAt);

  public LocalDateTime calculateEnd(LocalDateTime aggregatedAt) {
    return aggregatedAt;
  }
}
