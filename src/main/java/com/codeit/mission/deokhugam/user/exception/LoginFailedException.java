package com.codeit.mission.deokhugam.user.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

public class LoginFailedException extends DeokhugamException {
  public LoginFailedException() {
    super(ErrorCode.LOGIN_INPUT_INVALID);
  }
}
