package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class BookNotFoundException extends DeokhugamException {
  public BookNotFoundException() {
    super(ErrorCode.BOOK_NOT_FOUND);
  }
}
