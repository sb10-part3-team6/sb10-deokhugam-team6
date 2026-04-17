package com.codeit.mission.deokhugam.user.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends DeokhugamException {
  public UserNotFoundException(UUID userId) {
    super(ErrorCode.USER_NOT_FOUND, Map.of("userId", userId));
  }
}
