package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class DuplicatedIsbnException extends DeokhugamException {
    public DuplicatedIsbnException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicatedIsbnException(ErrorCode errorCode, Map<String, Object> details){
        super(errorCode, details);
    }
}
