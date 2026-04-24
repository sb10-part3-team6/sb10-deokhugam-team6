package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

public class IllegalLimitException extends DeokhugamException {
    public IllegalLimitException() {
        super(ErrorCode.ILLEGAL_LIMIT_VALUE);
    }
}