package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class DuplicateReviewException extends DeokhugamException {
    public DuplicateReviewException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateReviewException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
