package com.codeit.mission.deokhugam.book.exception;

import static com.codeit.mission.deokhugam.error.ErrorCode.S3_UPLOAD_FAILED;

import com.codeit.mission.deokhugam.error.DeokhugamException;

public class S3UploadFailureException extends DeokhugamException {

  public S3UploadFailureException() {
    super(S3_UPLOAD_FAILED);
  }
}
