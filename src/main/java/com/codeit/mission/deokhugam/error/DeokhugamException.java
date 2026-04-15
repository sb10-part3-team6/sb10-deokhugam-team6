package com.codeit.mission.deokhugam.error;

import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/*
    커스텀 예외를 위한 최상위 전역 클래스
 */
@Getter
public class DeokhugamException extends RuntimeException {
    private final Instant timestamp;                        // 에러 발생 시각
    private final ErrorCode errorCode;                      // 발생한 에러 코드
    private final Map<String, Object> details;              // 발생한 예외와 관련된 추가 정보

    // 생성자(에러 코드)
    public DeokhugamException(ErrorCode errorCode){
        this(errorCode, Collections.emptyMap());
    }

    // 생성자(에러 코드, 에러와 관련된 정보)
    public DeokhugamException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.timestamp = Instant.now();
        this.errorCode = errorCode;
        this.details = Collections.unmodifiableMap(details);
    }
}
