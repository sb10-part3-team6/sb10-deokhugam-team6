package com.codeit.mission.deokhugam.book.exception;


import static com.codeit.mission.deokhugam.error.ErrorCode.EXTERNAL_API_ERROR;

import com.codeit.mission.deokhugam.error.DeokhugamException;

public class ExternalApiErrorException extends DeokhugamException {

  public ExternalApiErrorException() {
    super(EXTERNAL_API_ERROR);
  }
}