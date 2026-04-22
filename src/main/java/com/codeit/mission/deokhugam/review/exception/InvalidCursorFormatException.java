package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class InvalidCursorFormatException extends DeokhugamException {

  public InvalidCursorFormatException(ErrorCode errorCode) {
    super(errorCode);
  }

  public InvalidCursorFormatException() {
    super(
        ErrorCode.INVALID_CURSOR_FORMAT
    );
  }
}
