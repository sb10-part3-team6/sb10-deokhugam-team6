package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class InvalidReviewRatingRangeException extends DeokhugamException {
    public InvalidReviewRatingRangeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidReviewRatingRangeException(ErrorCode errorCode, Map<String, Object> details){
        super(errorCode, details);
    }
}
