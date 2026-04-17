package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class InvalidIsbnException extends DeokhugamException {
    public InvalidIsbnException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidIsbnException(ErrorCode errorCode, Map<String, Object> details){
        super(errorCode, details);
    }
}
