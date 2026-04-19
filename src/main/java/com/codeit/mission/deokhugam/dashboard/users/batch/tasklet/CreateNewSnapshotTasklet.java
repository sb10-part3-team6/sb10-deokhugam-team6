package com.codeit.mission.deokhugam.dashboard.users.batch.tasklet;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.service.PowerUserSnapshotService;
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
@StepScope
@RequiredArgsConstructor
public class CreateNewSnapshotTasklet implements Tasklet {
  private final PowerUserSnapshotService powerUserSnapshotService;

  // 외부로부터 변수를 받아온다.
  @Value("#{jobParameters['periodType']}")
  private String periodTypeValue;

  @Value("#{jobParameters['aggregatedAt']}")
  private String aggregatedAtValue;

  @Value("#{jobParameters['snapshotId']}")
  private String snapshotIdValue;


  @Override
  public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    // 파싱
    PeriodType periodType = PeriodType.valueOf(periodTypeValue);
    LocalDateTime aggregatedAt = LocalDateTime.parse(aggregatedAtValue);
    UUID snapshotId = UUID.fromString(snapshotIdValue);

    powerUserSnapshotService.createStagingSnapshot(periodType,aggregatedAt,snapshotId);
    return RepeatStatus.FINISHED;
  }
}
