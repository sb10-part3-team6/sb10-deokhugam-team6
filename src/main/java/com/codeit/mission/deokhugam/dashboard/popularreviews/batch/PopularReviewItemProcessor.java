package com.codeit.mission.deokhugam.dashboard.popularreviews.batch;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.ReviewStat;
import com.codeit.mission.deokhugam.dashboard.popularreviews.entity.PopularReview;
import com.codeit.mission.deokhugam.dashboard.popularreviews.service.PopularReviewAggregateService;
import com.codeit.mission.deokhugam.dashboard.util.JobParameterUtils;
import com.codeit.mission.deokhugam.review.entity.Review;
import java.time.Instant;
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
  private Instant aggregatedAt;
  private UUID snapshotId;
  private Map<UUID, ReviewStat> statsByReviewId = Map.of();

  @BeforeStep
  void beforeStep(StepExecution stepExecution) {
    String periodTypeStr = stepExecution.getJobExecution().getJobParameters()
        .getString("periodType");
    String aggregatedAtStr = stepExecution.getJobExecution().getJobParameters()
        .getString("aggregatedAt");
    String snapshotIdStr = stepExecution.getJobExecution().getExecutionContext()
        .getString("snapshotId");

    JobParameterUtils.validateRequired(
        JobParameterUtils.parameter("periodType", periodTypeStr),
        JobParameterUtils.parameter("aggregatedAt", aggregatedAtStr),
        JobParameterUtils.parameter("snapshotId", snapshotIdStr)
    );

    this.periodType = JobParameterUtils.parseEnum("periodType", periodTypeStr, PeriodType.class);
    this.aggregatedAt = JobParameterUtils.parseInstant("aggregatedAt", aggregatedAtStr);
    this.snapshotId = JobParameterUtils.parseUuid("snapshotId", snapshotIdStr);

    this.statsByReviewId = popularReviewAggregateService.loadReviewStat(periodType, aggregatedAt);
  }


  @Override
  public @Nullable PopularReview process(@NonNull Review item) throws Exception {
    ReviewStat stat = statsByReviewId.get(item.getId());
    if (stat == null) {
      stat = popularReviewAggregateService.emptyStat(item.getId());
    }
    return popularReviewAggregateService.toPopularReview(
        item.getId(),
        stat,
        periodType,
        aggregatedAt,
        snapshotId
    );
  }
}
