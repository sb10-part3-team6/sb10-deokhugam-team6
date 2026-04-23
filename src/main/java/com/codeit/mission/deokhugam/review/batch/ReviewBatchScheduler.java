package com.codeit.mission.deokhugam.review.batch;

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

/*
    ReviewBatchScheduler
    --------------------
    ReviewBatchConfig에 정의된 물리 삭제 Job을 매일 정해진 시간에 실행시키는 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewBatchScheduler {

  private final JobLauncher jobLauncher;            // 배치 실행 엔진
  private final Job reviewHardDeleteJob;            // 리뷰 물리 삭제 작업

  // 매일 자정에 물리 삭제 배치 작업을 실행하는 스케줄러
  @Scheduled(cron = "0 0 0 * * *")
  public void runReviewHardDeleteJob() {
    try {
      // 실행할 때마다 새로운 파라미터를 생성
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("runTime", System.currentTimeMillis())
          .toJobParameters();

      // 스케줄러 실행
      JobExecution jobExecution = jobLauncher.run(reviewHardDeleteJob, jobParameters);

      if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
        log.info("[REVIEW_BATCH_SCHEDULER] Review Hard Delete Job has been completed.");
      } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
        log.error("[REVIEW_BATCH_SCHEDULER] Job execution failed. Job Status: {}",
            jobExecution.getStatus());
      } else {
        log.error("[REVIEW_BATCH_SCHEDULER] Job execution ended with status: {}",
            jobExecution.getStatus());
      }

    } catch (Exception e) {
      log.error("[REVIEW_BATCH_SCHEDULER] Job execution failed.", e);
    }
  }
}