package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class ReviewNotFoundException extends DuplicateReviewException{
    public ReviewNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public ReviewNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
