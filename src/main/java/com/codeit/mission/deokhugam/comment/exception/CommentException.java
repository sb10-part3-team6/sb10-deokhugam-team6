package com.codeit.mission.deokhugam.comment.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class CommentException extends DeokhugamException {
    public CommentException(ErrorCode errorCode) {
        super(errorCode);
    }

  public CommentException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
