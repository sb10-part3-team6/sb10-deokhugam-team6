package com.codeit.mission.deokhugam.dashboard.exceptions;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class DomainTypeNotEqualException extends DeokhugamException {

  public DomainTypeNotEqualException(Map<String,Object> details) {
    super(ErrorCode.DOMAIN_NOT_EQUAL, details);
  }
}
