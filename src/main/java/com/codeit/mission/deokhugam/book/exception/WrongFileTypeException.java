package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class WrongFileTypeException extends DeokhugamException {
    public WrongFileTypeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public WrongFileTypeException(ErrorCode errorCode, Map<String, Object> details){
        super(errorCode, details);
    }
}
