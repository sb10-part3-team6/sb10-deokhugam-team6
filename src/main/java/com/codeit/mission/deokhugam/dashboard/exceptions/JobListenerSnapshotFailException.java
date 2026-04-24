package com.codeit.mission.deokhugam.dashboard.exceptions;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class JobListenerSnapshotFailException extends DeokhugamException {

  public JobListenerSnapshotFailException(String snapshotIdValue) {
    super(ErrorCode.JOB_LISTENER_SNAPSHOT_FAIL, Map.of("SnapshotId", snapshotIdValue));
  }
}
