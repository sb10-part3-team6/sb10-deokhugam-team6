package com.codeit.mission.deokhugam.dashboard;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public enum PeriodType {
  // 일간
  DAILY {
    @Override
    public Instant calculateStart(Instant aggregatedAt) {
      return aggregatedAt.minus(1, ChronoUnit.DAYS); // 집계 날짜에서 하루를 뺀 날
    }
  },
  // 주간
  WEEKLY {
    @Override
    public Instant calculateStart(Instant aggregatedAt) {
      return aggregatedAt.minus(1, ChronoUnit.WEEKS); // 집계 날짜에서 한 주를 뺀 날짜
    }
  },
  // 월간
  MONTHLY {
    @Override
    public Instant calculateStart(Instant aggregatedAt) {
      return aggregatedAt.minus(1, ChronoUnit.MONTHS); // 집계 날짜에서 한 달을 뺀 날짜
    }
  },
  // 상시
  ALL_TIME {
    @Override
    public Instant calculateStart(Instant aggregatedAt) {
      return LocalDateTime.of(1970, 1, 1, 0, 0)
          .atZone(ZoneId.of("Asia/Seoul"))
          .toInstant();
    }
  };

  public abstract Instant calculateStart(Instant aggregatedAt);

  public Instant calculateEnd(Instant aggregatedAt) {
    return aggregatedAt;
  }
}
