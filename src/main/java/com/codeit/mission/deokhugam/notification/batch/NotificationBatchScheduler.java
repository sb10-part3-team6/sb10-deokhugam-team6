package com.codeit.mission.deokhugam.notification.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 알림 삭제 배치 실행을 위한 스케쥴러
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationBatchScheduler {

  private final JobLauncher jobLauncher;
  private final Job deleteOldNotificationsJob;

  // 매일 자정에 배치 작업 시작
  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  public void runJob() {

    try {
      JobParameters params = new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis()) // 중복 실행 방지
        .toJobParameters();

      // 실행
      JobExecution jobExecution = jobLauncher.run(deleteOldNotificationsJob, params);

      if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
        log.info("[NOTIFICATION_BATCH_SCHEDULER] Delete Old Notification Job has been completed.");
      } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
        log.error("[NOTIFICATION_BATCH_SCHEDULER] Job execution failed. Job Status: {}",
          jobExecution.getStatus());
      } else {
        log.error("[NOTIFICATION_BATCH_SCHEDULER] Job execution ended with status: {}",
          jobExecution.getStatus());
      }
    } catch (Exception e) {
      log.error("[NOTIFICATION_BATCH_SCHEDULER] Job execution failed.", e);
    }

  }
}