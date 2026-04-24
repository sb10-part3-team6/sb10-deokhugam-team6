package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

public class OcrFailedException extends DeokhugamException {

  public OcrFailedException() {
    super(ErrorCode.OCR_DETECT_FAILED);
  }
}