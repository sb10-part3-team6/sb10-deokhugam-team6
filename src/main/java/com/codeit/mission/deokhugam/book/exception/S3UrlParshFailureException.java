package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class S3UrlParshFailureException extends DeokhugamException {
    public S3UrlParshFailureException(){
        super(ErrorCode.S3_URL_PARSH_FAILED);
    }
}
