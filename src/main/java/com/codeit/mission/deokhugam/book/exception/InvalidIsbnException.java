package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class InvalidIsbnException extends DeokhugamException {

  public InvalidIsbnException(String isbn) {
    super(ErrorCode.INVALID_ISBN, Map.of("isbn", isbn));
  }
}
