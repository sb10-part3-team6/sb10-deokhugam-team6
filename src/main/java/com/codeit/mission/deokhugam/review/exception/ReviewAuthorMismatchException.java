package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class ReviewAuthorMismatchException extends DuplicateReviewException{
    public ReviewAuthorMismatchException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewAuthorMismatchException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
