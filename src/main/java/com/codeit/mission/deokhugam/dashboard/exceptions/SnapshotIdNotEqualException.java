package com.codeit.mission.deokhugam.dashboard.exceptions;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class SnapshotIdNotEqualException extends DeokhugamException {

  public SnapshotIdNotEqualException(
      Map<String, Object> details) {
    super(ErrorCode.SNAPSHOT_ID_NOT_EQUAL, details);
  }
}
