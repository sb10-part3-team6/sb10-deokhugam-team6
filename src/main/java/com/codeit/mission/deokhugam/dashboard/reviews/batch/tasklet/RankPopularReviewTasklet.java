package com.codeit.mission.deokhugam.dashboard.reviews.batch.tasklet;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidJobParameterException;
import com.codeit.mission.deokhugam.dashboard.reviews.service.PopularReviewAggregateService;
import com.codeit.mission.deokhugam.dashboard.reviews.service.PopularReviewService;
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
public class RankPopularReviewTasklet implements Tasklet {
  private final PopularReviewAggregateService popularReviewAggregateService;

  // 외부로부터 PeriodType 과 집계 날짜를 가져온다.
  @Value("#{jobParameters['periodType']}") private String periodTypeValue;
  @Value("#{jobParameters['aggregatedAt']}") private String aggregatedAtValue;
  @Value("#{jobExecutionContext['snapshotId']}") private String snapshotIdValue;

  @Override
  public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    popularReviewAggregateService.rankPopularReviews(
        getPeriodType(),
        getAggregatedAt(),
        getSnapshotId()
    );

    return RepeatStatus.FINISHED;
  }

  private UUID getSnapshotId() {
    if (snapshotIdValue == null || snapshotIdValue.isBlank()) {
      throw new InvalidJobParameterException("snapshotId");
    }
    return UUID.fromString(snapshotIdValue);
  }

  private LocalDateTime getAggregatedAt(){
    if(aggregatedAtValue == null || aggregatedAtValue.isBlank()){
      throw new InvalidJobParameterException("aggregatedAt");
    }
    return LocalDateTime.parse(aggregatedAtValue);
  }

  private PeriodType getPeriodType(){
    if(periodTypeValue == null || periodTypeValue.isBlank()){
      throw new InvalidJobParameterException("periodType");
    }
    return PeriodType.valueOf(periodTypeValue);
  }
}
