package com.codeit.mission.deokhugam.dashboard.popularreviews.batch;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidJobParameterException;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.ReviewStat;
import com.codeit.mission.deokhugam.dashboard.popularreviews.entity.PopularReview;
import com.codeit.mission.deokhugam.dashboard.popularreviews.service.PopularReviewAggregateService;
import com.codeit.mission.deokhugam.review.entity.Review;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
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
    String snapshotIdStr = stepExecution.getJobExecution().getExecutionContext().getString("snapshotId");

    Map<String, Object> details = new LinkedHashMap<>();
    if (periodTypeStr == null || periodTypeStr.isBlank()) {
      details.put("periodType", periodTypeStr != null ? periodTypeStr : "null");
    }

    if (aggregatedAtStr == null || aggregatedAtStr.isBlank()) {
      details.put("aggregatedAt", aggregatedAtStr != null ? aggregatedAtStr : "null");
    }

    if (snapshotIdStr == null || snapshotIdStr.isBlank()) {
      details.put("snapshotId", snapshotIdStr != null ? snapshotIdStr : "null");
    }

    if (!details.isEmpty()) {
      throw new InvalidJobParameterException(details);
    }

    try {
      this.periodType = PeriodType.valueOf(periodTypeStr);
    } catch (IllegalArgumentException e) {
      throw new InvalidJobParameterException(Map.of("periodType", periodTypeStr));
    }

    try {
      this.aggregatedAt = LocalDateTime.parse(aggregatedAtStr);
    } catch (DateTimeParseException e) {
      throw new InvalidJobParameterException(Map.of("aggregatedAt", aggregatedAtStr));
    }

    try {
      this.snapshotId = UUID.fromString(snapshotIdStr);
    } catch (IllegalArgumentException e) {
      throw new InvalidJobParameterException(Map.of("snapshotId", snapshotIdStr));
    }

    this.statsByReviewId = popularReviewAggregateService.loadReviewStat(periodType, aggregatedAt);
  }


  @Override
  public @Nullable PopularReview process(@NonNull Review item) throws Exception {
    ReviewStat stat = statsByReviewId.get(item.getId());
    if(stat == null) {
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
