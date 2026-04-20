package com.codeit.mission.deokhugam.dashboard.users.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class InvalidJobParameterException extends DeokhugamException {

  public InvalidJobParameterException(Object parameter) {
    super(ErrorCode.INVALID_JOB_PARAMETER, Map.of("parameter", parameter.toString()));
  }
}
