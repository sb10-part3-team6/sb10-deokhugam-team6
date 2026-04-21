package com.codeit.mission.deokhugam.user.batch;

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

// 탈퇴 유저 물리 삭제 배치를 실행하기 위한 스케줄러
@Slf4j
@Component
@RequiredArgsConstructor
public class UserHardDeleteScheduler {

    private final JobLauncher jobLauncher;
    private final Job userHardDeleteJob;

    // 매일 자정에 탈퇴 유저 물리 삭제 배치 작업을 실행함
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00:00에 실행
    public void runUserHardDeleteJob() {
        log.info("[Scheduler] 탈퇴 유저 물리 삭제 배치 작업을 시작합니다.");

        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // 매 실행마다 새로운 파라미터 부여
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(userHardDeleteJob, params);
            
            if (execution.getStatus() == BatchStatus.COMPLETED) {
                log.info("[Scheduler] 배치 작업이 성공적으로 실행되었습니다.");
            } else {
                log.error("[Scheduler] 배치 작업이 실패했습니다. 상태: {}", execution.getStatus());
            }
        } catch (Exception e) {
            log.error("[Scheduler] 배치 작업 실행 중 에러가 발생했습니다: {}", e.getMessage());
        }
    }
}
