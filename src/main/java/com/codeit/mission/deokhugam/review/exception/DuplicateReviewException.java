package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class DuplicateReviewException extends DeokhugamException {
    public DuplicateReviewException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateReviewException(UUID bookId, UUID userId) {
        super(
                ErrorCode.DUPLICATE_REVIEWS,
                Map.of(
                        "bookId", bookId,
                        "userId", userId
                )
        );
    }
}
