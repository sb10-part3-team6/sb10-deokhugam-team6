package com.codeit.mission.deokhugam.dashboard.popularreviews.batch;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidJobParameterException;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.ReviewStat;
import com.codeit.mission.deokhugam.dashboard.popularreviews.entity.PopularReview;
import com.codeit.mission.deokhugam.dashboard.popularreviews.service.PopularReviewAggregateService;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@StepScope
@Qualifier("reviewProcessor")
public class PopularReviewItemProcessor implements ItemProcessor<Review, PopularReview> {
  private final PopularReviewAggregateService popularReviewAggregateService;

  private PeriodType periodType;
  private LocalDateTime aggregatedAt;
  private UUID snapshotId;
  private Map<UUID, ReviewStat> statsByReviewId = Map.of();

  @BeforeStep
  void beforeStep(StepExecution stepExecution){
    String periodTypeStr = stepExecution.getJobExecution().getJobParameters().getString("periodType");
    String aggregatedAtStr = stepExecution.getJobExecution().getJobParameters().getString("aggregatedAt");
    String snapshotIdStr = stepExecution.getJobExecution().getJobParameters().getString("snapshotId");

    if (periodTypeStr == null || aggregatedAtStr == null || snapshotIdStr == null) {
      throw new InvalidJobParameterException(Map.of(
          "periodType", periodTypeStr != null ? periodTypeStr : null,
          "aggregatedAt", aggregatedAtStr != null ? aggregatedAtStr : null,
          "snapshotIdStr", snapshotIdStr != null ? snapshotIdStr : null
          ));
    }

    this.periodType = PeriodType.valueOf(periodTypeStr);
    this.aggregatedAt = LocalDateTime.parse(aggregatedAtStr);
    this.snapshotId = UUID.fromString(snapshotIdStr);
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
