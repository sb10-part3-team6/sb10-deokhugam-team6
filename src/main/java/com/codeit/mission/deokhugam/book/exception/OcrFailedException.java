package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;

import static com.codeit.mission.deokhugam.error.ErrorCode.EXTERNAL_API_ERROR;

public class OcrFailedException extends DeokhugamException {
    public OcrFailedException() {
        super(EXTERNAL_API_ERROR);
    }
}