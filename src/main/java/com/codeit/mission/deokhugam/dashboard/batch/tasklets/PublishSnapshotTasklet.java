package com.codeit.mission.deokhugam.dashboard.batch.tasklets;

import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidJobParameterException;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshotService;
import java.util.Map;
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
// Staging 된 새로 생성한 스냅샷 객체를 Publish로 바꾸는 태스크릿
public class PublishSnapshotTasklet implements Tasklet {
  private final AggregateSnapshotService aggregateSnapshotService;

  @Value("#{jobExecutionContext['snapshotId']}")
  private String snapshotIdValue;

  @Value("#{jobExecutionContext['domainType']}")
  private String domainTypeValue;

  @Override
  public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    // 입력 값 검증
    if(snapshotIdValue == null || snapshotIdValue.isBlank()){
      throw new InvalidJobParameterException(Map.of("snapshotId",
          snapshotIdValue != null ? snapshotIdValue : null));
    }

    // 스냅샷 Publish 메소드 호출
    aggregateSnapshotService.publishSnapshot(
        DomainType.valueOf(domainTypeValue),
        UUID.fromString(snapshotIdValue)
    );

    return RepeatStatus.FINISHED;
  }
}

