package com.codeit.mission.deokhugam.dashboard;

import java.time.LocalDateTime;

public enum PeriodType {
  DAILY {
    @Override
    public LocalDateTime calculateStart(LocalDateTime aggregatedAt) {
      return aggregatedAt.minusDays(1);
    }
  },
  WEEKLY {
    @Override
    public LocalDateTime calculateStart(LocalDateTime aggregatedAt) {
      return aggregatedAt.minusWeeks(1);
    }
  },
  MONTHLY {
    @Override
    public LocalDateTime calculateStart(LocalDateTime aggregatedAt) {
      return aggregatedAt.minusMonths(1);
    }
  },
  ALL_TIME {
    @Override
    public LocalDateTime calculateStart(LocalDateTime aggregatedAt) {
      return LocalDateTime.MIN;
    }
  };

  public abstract LocalDateTime calculateStart(LocalDateTime aggregatedAt);

  public LocalDateTime calculateEnd(LocalDateTime aggregatedAt) {
    return aggregatedAt;
  }
}
