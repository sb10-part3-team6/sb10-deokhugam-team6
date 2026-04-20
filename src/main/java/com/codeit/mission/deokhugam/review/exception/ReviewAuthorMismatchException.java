package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ReviewAuthorMismatchException extends DeokhugamException {
    public ReviewAuthorMismatchException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewAuthorMismatchException(UUID userId, UUID requestUserId) {
        super(
                ErrorCode.REVIEW_AUTHOR_MISMATCH,
                Map.of(
                        "reviewOwnerId", userId,
                        "requestUserId", requestUserId
                )
        );
    }
}
