package com.codeit.mission.deokhugam.dashboard.popularreviews.batch.tasklet;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularreviews.service.PopularReviewAggregateService;
import com.codeit.mission.deokhugam.dashboard.util.JobParameterUtils;
import java.time.Instant;
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
public class RankPopularReviewTasklet implements Tasklet {

  private final PopularReviewAggregateService popularReviewAggregateService;

  // 외부로부터 PeriodType 과 집계 날짜를 가져온다.
  @Value("#{jobParameters['periodType']}")
  private String periodTypeValue;
  @Value("#{jobParameters['aggregatedAt']}")
  private String aggregatedAtValue;
  @Value("#{jobExecutionContext['snapshotId']}")
  private String snapshotIdValue;

  @Override
  public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

    popularReviewAggregateService.rankPopularReviews(
        getPeriodType(),
        getAggregatedAt(),
        getSnapshotId()
    );

    return RepeatStatus.FINISHED;
  }

  private UUID getSnapshotId() {
    return JobParameterUtils.parseUuid("snapshotId", snapshotIdValue);
  }

  private Instant getAggregatedAt() {
    return JobParameterUtils.parseLocalDateTime("aggregatedAt", aggregatedAtValue);
  }

  private PeriodType getPeriodType() {
    return JobParameterUtils.parseEnum("periodType", periodTypeValue, PeriodType.class);
  }
}
