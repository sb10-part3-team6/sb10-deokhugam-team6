package com.codeit.mission.deokhugam.dashboard.users.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class SnapshotNotFoundException extends DeokhugamException {

  public SnapshotNotFoundException() {
    super(ErrorCode.SNAPSHOT_NOT_FOUND);
  }
}
