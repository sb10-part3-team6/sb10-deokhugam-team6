package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class WrongFileTypeException extends DeokhugamException {

  public WrongFileTypeException(String contentType) {
    super(ErrorCode.WRONG_FILE_TYPE, Map.of("contentType", contentType));
  }
}
