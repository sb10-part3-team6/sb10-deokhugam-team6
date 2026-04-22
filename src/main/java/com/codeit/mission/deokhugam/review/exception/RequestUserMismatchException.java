package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class RequestUserMismatchException extends DeokhugamException {

  public RequestUserMismatchException(ErrorCode errorCode) {
    super(errorCode);
  }

  public RequestUserMismatchException(UUID requestUserId, UUID headerRequestUserId) {
    super(
        ErrorCode.REQUEST_USER_MISMATCH,
        Map.of(
            "requestUserId in query", requestUserId,
            "requestUserId in header", headerRequestUserId
        )
    );
  }
}
