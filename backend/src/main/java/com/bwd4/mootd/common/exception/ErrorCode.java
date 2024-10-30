package com.bwd4.mootd.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCode {
    EXAMPLE(HttpStatus.FORBIDDEN,"E001","예시");

    private final int statusCode;
    private final String code;
    private final String message;


    ErrorCode(HttpStatus statusCode, String code, String message) {
        this.statusCode = statusCode.value();
        this.code = code;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
