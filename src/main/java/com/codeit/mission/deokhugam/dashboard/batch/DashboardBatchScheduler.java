package com.codeit.mission.deokhugam.dashboard.batch;

import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.exceptions.DashboardBatchJobFailException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// 대시보드 도메인 관련 배치 작업 스케쥴러
// Job을 총괄하는 역할?
public class DashboardBatchScheduler {

  private final JobLauncher jobLauncher;
  private final Job powerUserAggregationJob; // 파워 유저 집계 Job
  private final Job popularReviewAggregationJob; // 인기 리뷰 집계 Job
  private final Job popularBookAggregationJob; // 인기 도서 집계 Job

  // 매 00:00 에 배치 작업을 시작함.
  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  public void runDashboardAggregation() {
    // 집계 시작 시간은 00:00.00.0
    Instant aggregatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
        .toInstant();

    // DomainType을 순회
    for (DomainType domainType : List.of(
        DomainType.POWER_USER,
        DomainType.POPULAR_REVIEW,
        DomainType.POPULAR_BOOK)) {

      // Domain 별 주기를 순회
      for (PeriodType periodType : List.of(
          PeriodType.DAILY,
          PeriodType.WEEKLY,
          PeriodType.MONTHLY,
          PeriodType.ALL_TIME)) {

        // 한 도메인 내의 모든 기간에 속하는 데이터를 집계함.
        runJob(domainType, periodType, aggregatedAt);
      }
    }
  }

  // Job을 실행하는 실질적인 주체
  private void runJob(DomainType domainType, PeriodType periodType, Instant aggregatedAt) {
    // DomainType에 따라 Job이 달라진다.
    Job job = switch (domainType) {
      case POWER_USER -> powerUserAggregationJob;
      case POPULAR_REVIEW -> popularReviewAggregationJob;
      case POPULAR_BOOK -> popularBookAggregationJob;
    };

    // 집계에 필요한 파라미터들을 JobParameter로 전달한다.
    JobParameters params = new JobParametersBuilder()
        .addString("domainType", domainType.name())
        .addString("periodType", periodType.name())
        .addString("aggregatedAt", aggregatedAt.toString())
        .addLong("timestamp", System.currentTimeMillis())
        .toJobParameters();

    try {
      // 해당 Job을 파라미터와 함께 실행한다.
      jobLauncher.run(job, params);
    } catch (Exception e) {
      throw new DashboardBatchJobFailException(domainType, periodType, e);
    }
  }
}

