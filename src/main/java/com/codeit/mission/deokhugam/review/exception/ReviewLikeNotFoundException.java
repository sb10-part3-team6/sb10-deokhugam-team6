package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ReviewLikeNotFoundException extends DeokhugamException {
    public ReviewLikeNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewLikeNotFoundException(UUID reviewId, UUID userId) {
        super(
                ErrorCode.REVIEW_LIKE_NOT_FOUND,
                Map.of(
                        "reviewId", reviewId,
                        "userId", userId)
        );
    }
}
