package com.codeit.mission.deokhugam.book.exception;

import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;

import java.util.Map;

public class S3UploadFailureException extends DeokhugamException {
    public S3UploadFailureException(ErrorCode errorCode) {
        super(errorCode);
    }

    public S3UploadFailureException(ErrorCode errorCode, Map<String, Object> details){
        super(errorCode, details);
    }
}
