package com.codeit.mission.deokhugam.dashboard.exceptions;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class InvalidJobParameterException extends DeokhugamException {

  public InvalidJobParameterException(Map<String, Object> details) {
    super(ErrorCode.INVALID_JOB_PARAMETER, Map.of("parameter", details));
  }
}
