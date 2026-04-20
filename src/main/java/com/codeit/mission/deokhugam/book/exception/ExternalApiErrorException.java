package com.codeit.mission.deokhugam.book.exception;


import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static com.codeit.mission.deokhugam.error.ErrorCode.EXTERNAL_API_ERROR;

public class ExternalApiErrorException extends DeokhugamException {
    public ExternalApiErrorException() {
        super(EXTERNAL_API_ERROR);
    }
}