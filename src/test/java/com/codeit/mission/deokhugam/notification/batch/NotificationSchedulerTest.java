package com.codeit.mission.deokhugam.notification.batch;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
public class NotificationSchedulerTest {

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job deleteOldNotificationsJob;

  @InjectMocks
  private NotificationBatchScheduler notificationBatchScheduler;

  @Test
  @DisplayName("Spring Batch를 통한 확인 후 일주일이 경과한 알림 삭제 성공")
  void runDeleteOldNotificationsJobSuccess() throws Exception {
    // given
    JobExecution mockExecution = new JobExecution(1L);
    mockExecution.setStatus(BatchStatus.COMPLETED);

    given(jobLauncher.run(eq(deleteOldNotificationsJob), any(JobParameters.class)))
      .willReturn(mockExecution);

    // when
    notificationBatchScheduler.runJob();

    // then
    verify(jobLauncher, times(1)).run(eq(deleteOldNotificationsJob), any(JobParameters.class));
  }

  @Test
  @DisplayName("스케줄러 실행 중 예외가 발생해도 서버 종료 없이 예외를 처리")
  void runDeleteOldNotificationsJobExceptionHandled() throws Exception {
    // given
    given(jobLauncher.run(eq(deleteOldNotificationsJob), any(JobParameters.class)))
      .willThrow(new RuntimeException("Job execution failed."));

    // when & then
    assertDoesNotThrow(() -> notificationBatchScheduler.runJob());

    verify(jobLauncher, times(1)).run(eq(deleteOldNotificationsJob), any(JobParameters.class));
  }
}
