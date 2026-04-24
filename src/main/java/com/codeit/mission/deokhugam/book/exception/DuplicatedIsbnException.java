package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;

public class DuplicatedIsbnException extends DeokhugamException {

  public DuplicatedIsbnException(String isbn) {
    super(ErrorCode.DUPLICATE_ISBN, Map.of("isbn", isbn));
  }
}
