package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class CursorOrAfterFormatNotValidException extends DeokhugamException {
    public CursorOrAfterFormatNotValidException(){
        super(ErrorCode.CURSOR_OR_AFTER_FORMAT_NOT_VALID);
    }
}
