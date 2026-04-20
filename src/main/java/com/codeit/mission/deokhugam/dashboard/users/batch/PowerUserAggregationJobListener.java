package com.codeit.mission.deokhugam.dashboard.users.batch;

import com.codeit.mission.deokhugam.dashboard.users.service.PowerUserSnapshotService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PowerUserAggregationJobListener implements JobExecutionListener {

  private final PowerUserSnapshotService powerUserSnapshotService;

  @Override
  public void afterJob(JobExecution jobExecution) {
    if (jobExecution.getStatus() != BatchStatus.FAILED) {
      return;
    }

    ExecutionContext executionContext = jobExecution.getExecutionContext();
    if (!executionContext.containsKey("snapshotId")) {
      return;
    }

    String snapshotIdValue = executionContext.getString("snapshotId");
    if (snapshotIdValue.isBlank()) {
      return;
    }

    powerUserSnapshotService.failSnapshot(UUID.fromString(snapshotIdValue));
  }
}
