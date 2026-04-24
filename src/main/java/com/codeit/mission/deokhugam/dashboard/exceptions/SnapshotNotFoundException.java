package com.codeit.mission.deokhugam.dashboard.exceptions;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

public class SnapshotNotFoundException extends DeokhugamException {

  public SnapshotNotFoundException() {
    super(ErrorCode.SNAPSHOT_NOT_FOUND);
  }
}
