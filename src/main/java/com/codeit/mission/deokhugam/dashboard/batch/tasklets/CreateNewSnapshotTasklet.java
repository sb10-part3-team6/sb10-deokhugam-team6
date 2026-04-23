package com.codeit.mission.deokhugam.dashboard.batch.tasklets;

import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidJobParameterException;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshot;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshotService;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class CreateNewSnapshotTasklet implements Tasklet {
  private final AggregateSnapshotService aggregateSnapshotService;

  // ?몃?濡쒕???蹂?섎? 諛쏆븘?⑤떎.
  @Value("#{jobParameters['periodType']}")
  private String periodTypeValue;

  @Value("#{jobParameters['aggregatedAt']}")
  private String aggregatedAtValue;

  @Value("#{jobParameters['domainType']}")
  private String domainTypeValue;

  @Override
  public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

    PeriodType periodType = getPeriodType();
    LocalDateTime aggregatedAt = getAggregatedAt();
    DomainType domainType = getDomainType();

    // ?ㅻ깄??媛앹껜 ?앹꽦 ?쒕퉬??硫붿꽌?쒕? ?몄텧??
    AggregateSnapshot snapshot =
        aggregateSnapshotService.createStagingSnapshot(domainType, periodType, aggregatedAt);


    // ?ㅼ쓬 ?ㅽ뀦??AggregateStep???ㅻ깄??Id瑜??꾨떖?섍린 ?꾪빐 jobExecutionContext??媛믪쓣 ??ν븿.
    ExecutionContext context = chunkContext.getStepContext() // ?꾩옱 ?ㅽ뻾 以묒씤 Step??而⑦뀓?ㅽ듃 ?뺣낫 媛?몄샂
        .getStepExecution() // ?꾩옱 Step ?ㅽ뻾 媛앹껜
        .getJobExecution() // ???랁븳 ?꾩껜 Job ?ㅽ뻾 媛앹껜
        .getExecutionContext(); // Job ?꾩껜媛 怨듭쑀?섎뒗 Key-Value ??μ냼

    // 而⑦뀓?ㅽ듃???ㅻ깄??Id, ?꾨찓??醫낅쪟瑜????
    context.putString("snapshotId", snapshot.getSnapshotId().toString()); // ??snapshotId ?대쫫?쇰줈 ?대떦 Id瑜????
    context.putString("domainType", domainType.name()); // domainType ?대쫫?쇰줈 ?대떦 ?꾨찓??醫낅쪟瑜????

    // ?ㅽ뀦 醫낅즺瑜??뚮┝
    return RepeatStatus.FINISHED;
  }

  private PeriodType getPeriodType() {
    if (periodTypeValue == null || periodTypeValue.isBlank()) {
      throw new InvalidJobParameterException(
          Map.of("periodType", periodTypeValue != null ? periodTypeValue : "null"));
    }

    try {
      return PeriodType.valueOf(periodTypeValue);
    } catch (IllegalArgumentException e) {
      throw new InvalidJobParameterException(Map.of("periodType", periodTypeValue));
    }
  }

  private LocalDateTime getAggregatedAt() {
    if (aggregatedAtValue == null || aggregatedAtValue.isBlank()) {
      throw new InvalidJobParameterException(
          Map.of("aggregatedAt", aggregatedAtValue != null ? aggregatedAtValue : "null"));
    }

    try {
      return LocalDateTime.parse(aggregatedAtValue);
    } catch (DateTimeParseException e) {
      throw new InvalidJobParameterException(Map.of("aggregatedAt", aggregatedAtValue));
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
