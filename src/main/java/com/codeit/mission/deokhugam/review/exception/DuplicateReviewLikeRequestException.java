package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class DuplicateReviewLikeRequestException extends DeokhugamException {
    public DuplicateReviewLikeRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateReviewLikeRequestException(UUID reviewId, UUID requestUserId) {
        super(
                ErrorCode.DUPLICATE_REVIEW_LIKE_REQUEST,
                Map.of(
                        "reviewId", reviewId,
                        "requestUserId", requestUserId
                )
        );
    }
}
