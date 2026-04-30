package com.codeit.mission.deokhugam.dashboard.batch;

import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshotService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
// 실패를 감지하고 행동하는 실패 Job 리스너
public class DashboardAggregationJobListener implements JobExecutionListener {

  private final AggregateSnapshotService aggregateSnapshotService;

  // 작업 이후 Status를 확인하고 처리하는 역할
  @Override
  public void afterJob(JobExecution jobExecution) {
    log.info("[DASHBOARD_JOB_AFTER] jobName={}, status={}, exitStatus={}",
        jobExecution.getJobInstance().getJobName(),
        jobExecution.getStatus(),
        jobExecution.getExitStatus().getExitCode());
    // Failed가 아니면 넘어감
    if (jobExecution.getStatus() != BatchStatus.FAILED) {
      return;
    }

    // 컨텍스트에서 스냅샷 Id를 구해온다.
    ExecutionContext context = jobExecution.getExecutionContext();
    String snapshotIdValue = context.getString("snapshotId", null);

    // 스냅샷 ID가 아직 생성이 안됨 -> CreateSnapshot에서 실패가 일어남 -> Fail 처리 할 스냅샷이 없으므로 넘어감
    if (snapshotIdValue == null || snapshotIdValue.isBlank()) {
      log.warn("[DASHBOARD_JOB_FAILED_NO_SNAPSHOT] jobName={}",
          jobExecution.getJobInstance().getJobName());
      return;
    }

    try {
      // 스냅샷을 Fail 처리 시도
      aggregateSnapshotService.failSnapshot(UUID.fromString(snapshotIdValue));
      log.info("[DASHBOARD_SNAPSHOT_FAILED] snapshotId={}", snapshotIdValue);
    } catch (IllegalArgumentException ignored) {
      log.warn("[DASHBOARD_SNAPSHOT_FAIL_SKIPPED] invalidSnapshotId={}", snapshotIdValue);
    }
  }
}

