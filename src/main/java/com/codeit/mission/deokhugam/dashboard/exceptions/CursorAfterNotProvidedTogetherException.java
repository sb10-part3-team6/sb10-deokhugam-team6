package com.codeit.mission.deokhugam.dashboard.users.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

public class CursorAfterNotProvidedTogetherException extends DeokhugamException {

  public CursorAfterNotProvidedTogetherException() {
    super(ErrorCode.CURSOR_AFTER_NOT_PROVIDED_TOGETHER);
  }
}
