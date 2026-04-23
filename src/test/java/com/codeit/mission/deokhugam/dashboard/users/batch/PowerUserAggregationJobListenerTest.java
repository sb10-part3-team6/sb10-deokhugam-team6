//package com.codeit.mission.deokhugam.dashboard.users.batch;
//
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.codeit.mission.deokhugam.dashboard.powerusers.service.PowerUserSnapshotService;
//import java.util.UUID;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.batch.core.BatchStatus;
//import org.springframework.batch.core.JobExecution;
//import org.springframework.batch.item.ExecutionContext;
//
//@ExtendWith(MockitoExtension.class)
//class PowerUserAggregationJobListenerTest {
//
//  @Mock
//  private JobExecution jobExecution;
//
//  @Mock
//  private PowerUserSnapshotService powerUserSnapshotService;
//
//  @InjectMocks
//  private PowerUserAggregationJobListener powerUserAggregationJobListener;
//
//  @Test
//  @DisplayName("job 이 FAILED 이고 snapshotId 가 있으면 snapshot 을 FAILED 로 변경한다")
//  void afterJob_failedWithSnapshotId_marksSnapshotFailed() {
//    // given
//    UUID snapshotId = UUID.randomUUID();
//    ExecutionContext executionContext = new ExecutionContext();
//    executionContext.putString("snapshotId", snapshotId.toString()); // 컨텍스트에 snapshotId를 주입한다.
//
//    when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED); // Batch 작업 상태는 FAILED
//    when(jobExecution.getExecutionContext()).thenReturn(executionContext); // 컨텍스트를 리턴함.
//
//    // when
//    powerUserAggregationJobListener.afterJob(jobExecution);
//
//    verify(powerUserSnapshotService).failSnapshot(snapshotId);
//  }
//
//  @Test
//  @DisplayName("job이 FAILED 이여도 snapshotId 가 공백이면 snapshot 상태를 변경하지 않는다.")
//  void afterJob_failed_doesNotMarkSnapshotFailed(){
//    // given
//    ExecutionContext executionContext = new ExecutionContext();
//    executionContext.putString("snapshotId", "  "); // snapshotId를 공백으로 둠
//
//    when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED); // Job 상태 -> FAILED
//    when(jobExecution.getExecutionContext()).thenReturn(executionContext);
//
//    // when
//    powerUserAggregationJobListener.afterJob(jobExecution);
//
//    // then
//    verify(powerUserSnapshotService, never()).failSnapshot(org.mockito.ArgumentMatchers.any());
//
//
//
//  }
//
//  @Test
//  @DisplayName("job 이 FAILED 가 아니면 snapshot 상태를 변경하지 않는다")
//  void afterJob_notFailed_doesNotMarkSnapshotFailed() {
//    when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
//
//    powerUserAggregationJobListener.afterJob(jobExecution);
//
//    verify(powerUserSnapshotService, never()).failSnapshot(org.mockito.ArgumentMatchers.any());
//  }
//
//  @Test
//  @DisplayName("job 이 FAILED 여도 snapshotId 가 없으면 snapshot 상태를 변경하지 않는다")
//  void afterJob_failedWithoutSnapshotId_doesNotMarkSnapshotFailed() {
//    when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED);
//    when(jobExecution.getExecutionContext()).thenReturn(new ExecutionContext());
//
//    powerUserAggregationJobListener.afterJob(jobExecution);
//
//    verify(powerUserSnapshotService, never()).failSnapshot(org.mockito.ArgumentMatchers.any());
//  }
//}
