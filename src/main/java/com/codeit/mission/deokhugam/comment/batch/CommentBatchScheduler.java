package com.codeit.mission.deokhugam.comment.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job commentHardDeleteJob;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void runCommentHardDeleteJob() {
        log.info("[Scheduler] Comment Hard Delete Job is running.");
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())    // 매 실행마다 새로운 파라미터 부여
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(commentHardDeleteJob, params);

            if (execution.getStatus() == BatchStatus.COMPLETED) {
                log.info("[Scheduler] Comment Hard Delete Job has been completed.");
            } else {
                log.error("[Scheduler] Comment Hard Delete Job failed. Status: {}", execution.getStatus());
            }
        } catch (Exception e) {
            log.error("[Scheduler] Comment Hard Delete Job failed: {}", e.getMessage());
        }
    }
}
