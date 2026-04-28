package com.codeit.mission.deokhugam.dashboard.popularbooks.batch.tasklet;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.request.PopularBookStat;
import com.codeit.mission.deokhugam.dashboard.popularbooks.entity.PopularBook;
import com.codeit.mission.deokhugam.dashboard.popularbooks.service.PopularBookAggregationService;
import com.codeit.mission.deokhugam.dashboard.util.JobParameterUtils;
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
@StepScope
@RequiredArgsConstructor
@Qualifier("bookProcessor")
public class PopularBookItemProcessor implements ItemProcessor<Book, PopularBook> {

  private final PopularBookAggregationService popularBookAggregateService;

  private PeriodType periodType;
  private Instant aggregatedAt;
  private UUID snapshotId;
  private Map<UUID, PopularBookStat> statsByBookId = Map.of();

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

    this.statsByBookId = popularBookAggregateService.loadBookStat(periodType, aggregatedAt);
  }

  @Override
  public @Nullable PopularBook process(@NonNull Book item) {
    PopularBookStat stat = statsByBookId.get(item.getId());
    if (stat == null) {
      stat = popularBookAggregateService.emptyStat(item.getId());
    }
    return popularBookAggregateService.toPopularBook(
        item.getId(),
        stat,
        periodType,
        aggregatedAt,
        snapshotId
    );
  }
}
