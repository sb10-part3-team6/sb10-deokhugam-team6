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
// Staging ???덈줈 ?앹꽦???ㅻ깄??媛앹껜瑜?Publish濡?諛붽씀???쒖뒪?щ┸
public class PublishSnapshotTasklet implements Tasklet {
  private final AggregateSnapshotService aggregateSnapshotService;

  @Value("#{jobExecutionContext['snapshotId']}")
  private String snapshotIdValue;

  @Value("#{jobExecutionContext['domainType']}")
  private String domainTypeValue;

  @Override
  public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    // ?ㅻ깄??Publish 硫붿냼???몄텧
    aggregateSnapshotService.publishSnapshot(
        getDomainType(),
        getSnapshotId()
    );

    return RepeatStatus.FINISHED;
  }

  private UUID getSnapshotId() {
    if (snapshotIdValue == null || snapshotIdValue.isBlank()) {
      throw new InvalidJobParameterException(
          Map.of("snapshotId", snapshotIdValue != null ? snapshotIdValue : "null"));
    }

    try {
      return UUID.fromString(snapshotIdValue);
    } catch (IllegalArgumentException e) {
      throw new InvalidJobParameterException(Map.of("snapshotId", snapshotIdValue));
    }
  }

  private DomainType getDomainType() {
    if (domainTypeValue == null || domainTypeValue.isBlank()) {
      throw new InvalidJobParameterException(
          Map.of("domainType", domainTypeValue != null ? domainTypeValue : "null"));
    }

    try {
      return DomainType.valueOf(domainTypeValue);
    } catch (IllegalArgumentException e) {
      throw new InvalidJobParameterException(Map.of("domainType", domainTypeValue));
    }
  }
}
