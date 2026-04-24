package com.codeit.mission.deokhugam.dashboard.exceptions;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class SnapshotNotStagedPublishException extends DeokhugamException {

  public SnapshotNotStagedPublishException(Map<String,Object> details) {
    super(ErrorCode.SNAPSHOT_NOT_STAGE_BUT_PUBLISH, details);
  }
}
