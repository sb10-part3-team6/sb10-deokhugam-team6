package com.codeit.mission.deokhugam.dashboard.users.batch.tasklet;

import com.codeit.mission.deokhugam.dashboard.users.exception.InvalidJobParameterException;
import com.codeit.mission.deokhugam.dashboard.users.service.PowerUserSnapshotService;
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
public class PublishSnapshotTasklet implements Tasklet {
  private final PowerUserSnapshotService powerUserSnapshotService;

  // Context에 의해 주입받음
  @Value("#{jobExecutionContext['snapshotId']}")
  private String snapshotIdValue;

  @Override
  public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    powerUserSnapshotService.publishSnapshot(getSnapshotId());

    return RepeatStatus.FINISHED;
  }

  // 파라미터로 들어온 snapshotId를 검증
  private UUID getSnapshotId() {
    if (snapshotIdValue == null || snapshotIdValue.isBlank()) {
      throw new InvalidJobParameterException("snapshotId");
    }
    return UUID.fromString(snapshotIdValue);
  }
}
