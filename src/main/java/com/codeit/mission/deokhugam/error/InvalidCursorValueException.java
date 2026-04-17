package com.codeit.mission.deokhugam.error;

import java.util.Map;

public class InvalidCursorValueException extends DeokhugamException{

  public InvalidCursorValueException() {
    super(ErrorCode.CURSOR_OR_AFTER_FORMAT_NOT_VALID);
  }

  public InvalidCursorValueException(Map<String, Object> details) {
    super(ErrorCode.CURSOR_OR_AFTER_FORMAT_NOT_VALID, details);
  }
}
