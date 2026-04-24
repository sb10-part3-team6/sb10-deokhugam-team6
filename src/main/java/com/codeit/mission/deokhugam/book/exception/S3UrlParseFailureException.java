package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

public class S3UrlParseFailureException extends DeokhugamException {

  public S3UrlParseFailureException() {
    super(ErrorCode.S3_URL_PARSE_FAILED);
  }
}
