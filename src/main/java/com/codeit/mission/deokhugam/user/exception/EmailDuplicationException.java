package com.codeit.mission.deokhugam.user.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class EmailDuplicationException extends DeokhugamException {
  public EmailDuplicationException(String email) {
    super(ErrorCode.EMAIL_DUPLICATION, Map.of("email", email));
  }
}
