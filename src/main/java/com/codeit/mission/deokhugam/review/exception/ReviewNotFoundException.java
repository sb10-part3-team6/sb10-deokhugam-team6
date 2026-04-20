package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ReviewNotFoundException extends DeokhugamException {
    public ReviewNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewNotFoundException(UUID reviewId) {
        super(
                ErrorCode.REVIEW_NOT_FOUND,
                Map.of("reviewId", reviewId)
        );
    }
}
