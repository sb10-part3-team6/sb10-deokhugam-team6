package com.codeit.mission.deokhugam.dashboard.users;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.service.PowerUserService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PowerUserJobTasklet implements Tasklet {

  private static final ZoneId BATCH_ZONE = ZoneId.of("Asia/Seoul");

  private final PowerUserService powerUserService;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    LocalDateTime aggregatedAt = LocalDate.now(BATCH_ZONE).atStartOfDay();

    powerUserService.aggregate(PeriodType.DAILY, aggregatedAt);

    if (aggregatedAt.getDayOfWeek() == DayOfWeek.MONDAY) {
      powerUserService.aggregate(PeriodType.WEEKLY, aggregatedAt);
    }

    if (aggregatedAt.getDayOfMonth() == 1) {
      powerUserService.aggregate(PeriodType.MONTHLY, aggregatedAt);
    }

    return RepeatStatus.FINISHED;
  }
}
