package com.codeit.mission.deokhugam.dashboard.users.batch;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PowerUserBatchScheduler {
  private final JobLauncher jobLauncher;
  private final Job powerUserAggregationJob; // Bean 주입

  // 매일 00:00 마다 Job을 수행한다.
  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  public void runDailyAggregation() throws Exception{
    LocalDateTime aggregatedAt = LocalDateTime.now()
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0); // 집계 일자는 매일 00:00 나노초까지 0으로 초기화

    runJob(PeriodType.DAILY, aggregatedAt);
    runJob(PeriodType.WEEKLY, aggregatedAt);
    runJob(PeriodType.MONTHLY, aggregatedAt);
    runJob(PeriodType.ALL_TIME, aggregatedAt);
  }

  private void runJob(PeriodType periodType, LocalDateTime aggregatedAt)
      throws Exception {
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("periodType", periodType.name())
        .addString("aggregatedAt", aggregatedAt.toString())
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters();

    jobLauncher.run(powerUserAggregationJob, jobParameters);
  }

}
