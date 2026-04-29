package com.codeit.mission.deokhugam.user.batch;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
class UserHardDeleteSchedulerTest {

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job userHardDeleteJob;

  @InjectMocks
  private UserHardDeleteScheduler scheduler;

  @Test
  @DisplayName("스케줄러 실행 시 JobLauncher가 정확히 1회 호출되는지 확인")
  void runUserHardDeleteJob_CallsJobLauncherOnce() throws Exception {
    // given
    JobExecution successExecution = mockJobExecution(BatchStatus.COMPLETED);
    when(jobLauncher.run(eq(userHardDeleteJob), any(JobParameters.class)))
        .thenReturn(successExecution);

    // when
    scheduler.runUserHardDeleteJob();

    // then
    verify(jobLauncher, times(1)).run(eq(userHardDeleteJob), any(JobParameters.class));
  }

  @Test
  @DisplayName("스케줄러 실행 시 매 실행마다 다른 JobParameters가 전달되는지 확인 (중복 실행 방지)")
  void runUserHardDeleteJob_UsesDifferentParamsEachTime() throws Exception {
    // given
    JobExecution execution = mockJobExecution(BatchStatus.COMPLETED);
    when(jobLauncher.run(eq(userHardDeleteJob), any(JobParameters.class)))
        .thenReturn(execution);

    // when - 두 번 실행 (time 파라미터가 달라지도록 1ms 대기)
    scheduler.runUserHardDeleteJob();
    Thread.sleep(1);
    scheduler.runUserHardDeleteJob();

    // then - JobLauncher가 2회 호출되고, 파라미터(time)가 달라야 함
    ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
    verify(jobLauncher, times(2)).run(eq(userHardDeleteJob), paramsCaptor.capture());
    List<JobParameters> captured = paramsCaptor.getAllValues();
    assertNotEquals(captured.get(0).getLong("time"), captured.get(1).getLong("time"));
  }

  @Test
  @DisplayName("Job 실행 중 예외 발생 시 스케줄러가 예외를 삼키고 정상 종료되는지 확인")
  void runUserHardDeleteJob_DoesNotPropagateException_WhenJobFails() throws Exception {
    // given
    when(jobLauncher.run(eq(userHardDeleteJob), any(JobParameters.class)))
        .thenThrow(new RuntimeException("배치 실행 오류"));

    // when & then - 예외가 외부로 전파되지 않아야 함
    assertDoesNotThrow(() -> scheduler.runUserHardDeleteJob());
  }

  @Test
  @DisplayName("Job이 FAILED 상태로 종료돼도 스케줄러가 예외를 던지지 않는지 확인")
  void runUserHardDeleteJob_DoesNotThrow_WhenJobStatusFailed() throws Exception {
    // given
    JobExecution failedExecution = mockJobExecution(BatchStatus.FAILED);
    when(jobLauncher.run(eq(userHardDeleteJob), any(JobParameters.class)))
        .thenReturn(failedExecution);

    // when & then
    assertDoesNotThrow(() -> scheduler.runUserHardDeleteJob());
  }

  private JobExecution mockJobExecution(BatchStatus status) {
    JobExecution execution = org.mockito.Mockito.mock(JobExecution.class);
    when(execution.getStatus()).thenReturn(status);
    return execution;
  }
}
