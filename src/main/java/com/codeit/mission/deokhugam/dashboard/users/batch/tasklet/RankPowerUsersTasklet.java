package com.codeit.mission.deokhugam.dashboard.users.batch.tasklet;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.service.PowerUserAggregateService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@StepScope
public class RankPowerUsersTasklet implements Tasklet {
  private final PowerUserAggregateService powerUserAggregateService;

  // 외부로부터 PeriodType 과 집계 날짜를 가져온다.
  @Value("#{jobParameters['periodType']}") private String periodTypeValue;
  @Value("#{jobParameters['aggregatedAt']}") private String aggregatedAtValue;
  @Value("#{jobParameters['snapshotId']}") private String snapshotIdValue;

  @Override
  public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {

    LocalDateTime aggregatedAt = LocalDateTime.parse(aggregatedAtValue);
    PeriodType periodType = PeriodType.valueOf(periodTypeValue);
    UUID snapshotId = UUID.fromString(snapshotIdValue);

    powerUserAggregateService.rankPowerUsers(periodType, aggregatedAt, snapshotId);

    return RepeatStatus.FINISHED;
  }
}
