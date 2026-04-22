package com.codeit.mission.deokhugam.dashboard.reviews.batch;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.reviews.dto.ReviewStat;
import com.codeit.mission.deokhugam.dashboard.reviews.entity.PopularReview;
import com.codeit.mission.deokhugam.dashboard.reviews.service.PopularReviewAggregateService;
import com.codeit.mission.deokhugam.review.entity.Review;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@StepScope
public class PopularReviewItemProcessor implements ItemProcessor<Review, PopularReview> {
  private final PopularReviewAggregateService popularReviewAggregateService;

  private PeriodType periodType;
  private LocalDateTime aggregatedAt;
  private UUID snapshotId;
  private Map<UUID, ReviewStat> statsByReviewId = Map.of();

  @BeforeStep
  void beforeStep(StepExecution stepExecution){
    this.periodType = PeriodType.valueOf(stepExecution.getJobExecution().getJobParameters().getString("periodType"));
    this.aggregatedAt = LocalDateTime.parse(stepExecution.getJobExecution().getJobParameters().getString("aggregatedAt"));
    this.snapshotId = UUID.fromString(stepExecution.getJobExecution().getExecutionContext().getString("snapshotId"));
    this.statsByReviewId = popularReviewAggregateService.loadReviewStat(periodType, aggregatedAt);
  }


  @Override
  public @Nullable PopularReview process(@NonNull Review item) throws Exception {
    ReviewStat stat = statsByReviewId.getOrDefault(item.getId(), popularReviewAggregateService.emptyStat(item.getId()));
    return popularReviewAggregateService.toPopularReview(
        item.getId(),
        stat,
        periodType,
        aggregatedAt,
        snapshotId
    );
  }
}
