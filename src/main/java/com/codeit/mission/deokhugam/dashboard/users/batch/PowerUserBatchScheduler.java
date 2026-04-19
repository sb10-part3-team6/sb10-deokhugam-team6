package com.codeit.mission.deokhugam.dashboard.users.batch;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.exception.PowerUserAggregationFailed;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
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
    LocalDateTime aggregatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0); // 집계 일자는 매일 00:00 나노초까지 0으로 초기화



    // Period 별로 Job을 런치하는데, 특정 Period의 job이 실패하면 예외를 던진다.
    for(PeriodType periodType : List.of(PeriodType.DAILY, PeriodType.WEEKLY, PeriodType.MONTHLY,
        PeriodType.ALL_TIME)){
      UUID snapshotId = UUID.randomUUID();

      try{
        runJob(periodType, aggregatedAt, snapshotId);
      } catch (Exception e){
        throw new PowerUserAggregationFailed(periodType);
      }
    }
  }

  // 실질적으로 PowerUserAggregateJob을 수행하는 메서드
  private void runJob(PeriodType periodType, LocalDateTime aggregatedAt, UUID snapshotId)
      throws Exception {
    // Job에 넘겨줄 파라미터를 주입한다.
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("periodType", periodType.name())
        .addString("aggregatedAt", aggregatedAt.toString())
        .addString("snapshotId", snapshotId.toString(), false)
        .toJobParameters();

    jobLauncher.run(powerUserAggregationJob, jobParameters);
  }

}
