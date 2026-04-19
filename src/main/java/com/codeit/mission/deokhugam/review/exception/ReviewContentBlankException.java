package com.codeit.mission.deokhugam.review.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class ReviewContentBlankException extends DeokhugamException {
    public ReviewContentBlankException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReviewContentBlankException(String content) {
        super(
                ErrorCode.REVIEW_CONTENT_BLANK,
                Map.of("content", content == null ? "null" : content)
        );
    }
}
