package com.codeit.mission.deokhugam.dashboard.popularreviews.batch.tasklet;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidJobParameterException;
import com.codeit.mission.deokhugam.dashboard.popularreviews.service.PopularReviewAggregateService;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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

    // parsing 메서드에서 나오는 예외를 처리함.
    try{
      popularReviewAggregateService.rankPopularReviews(
          getPeriodType(),
          getAggregatedAt(),
          getSnapshotId()
      );
    }catch(IllegalArgumentException e){
      throw new InvalidJobParameterException(Map.of("periodType",periodTypeValue,
          "snapshotId", snapshotIdValue));
    }
    catch(DateTimeParseException e){
      throw new InvalidJobParameterException(Map.of("AggregatedAt", aggregatedAtValue));
    }

    return RepeatStatus.FINISHED;
  }

  private UUID getSnapshotId(){
    if (snapshotIdValue == null || snapshotIdValue.isBlank()) {
      if (snapshotIdValue != null) {
        throw new InvalidJobParameterException(Map.of("snapshotId", snapshotIdValue));
      }
    }
    return UUID.fromString(snapshotIdValue != null ? snapshotIdValue : null);
  }

  private LocalDateTime getAggregatedAt(){
    if(aggregatedAtValue == null || aggregatedAtValue.isBlank()){
      if (aggregatedAtValue != null) {
        throw new InvalidJobParameterException(Map.of("aggregatedAt", aggregatedAtValue));
      }
    }
    return LocalDateTime.parse(aggregatedAtValue != null ? aggregatedAtValue : null);
  }

  private PeriodType getPeriodType(){
    if(periodTypeValue == null || periodTypeValue.isBlank()){
      if (periodTypeValue != null) {
        throw new InvalidJobParameterException(Map.of("periodType",periodTypeValue));
      }
    }
    return PeriodType.valueOf(periodTypeValue);
  }
}
