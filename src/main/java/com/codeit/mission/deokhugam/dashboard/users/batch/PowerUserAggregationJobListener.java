package com.codeit.mission.deokhugam.dashboard.users.batch;

import com.codeit.mission.deokhugam.dashboard.users.service.PowerUserSnapshotService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

// Job이 끝난 뒤 성공이면 아무것도 안함
// 끝난 뒤실패면 snapshotId 찾아서 FAIL 처리함.
@Component
@RequiredArgsConstructor
public class PowerUserAggregationJobListener implements JobExecutionListener {

  private final PowerUserSnapshotService powerUserSnapshotService;

  // 배치 작업 시작 전 실행되는 메서드
  @Override
  public void afterJob(JobExecution jobExecution) { // JobExecution => 이번 배치 실행 1회분의 실행 기록 객체
    /*
    JobExecution은 이번 배치 실행 1회분의 실행 기록 객체
      성공했는지 실패했는지
      시작시간
      종료시간
      파라미터
      context 데이터
      step 결과들
     전부 담고 있음.
     */

    if (jobExecution.getStatus() != BatchStatus.FAILED) { // 배치의 Status가 Failed가 아니라면 그냥 넘어감.
      return;
    }

    /*
    ExecutionContext는 배치 실행 중 공유하는 Map 저장소
      Job 중간에 데이터 저장 가능.
      ex) executionContext.getString("snapshotId", uuid);
      Step2, afterJob 등에서 다시 사용 가능
     */
    ExecutionContext executionContext = jobExecution.getExecutionContext();
    if (!executionContext.containsKey("snapshotId")) { // 스냅샷이 존재하지 않으면 그냥 넘어감.
      return;
    }

    // 스냅샷이 Null이거나 공백이면 넘어감.
    String snapshotIdValue = executionContext.getString("snapshotId");
    if (snapshotIdValue == null || snapshotIdValue.isBlank()) {
      return;
    }

    // 스냅샷 id가 존재하면 SnapshotService 계층에서 해당 Job Fail처리함.
    powerUserSnapshotService.failSnapshot(UUID.fromString(snapshotIdValue));
  }
}
