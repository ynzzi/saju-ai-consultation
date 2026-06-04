package com.portfolio.saju.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "Too many requests"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "Email already exists"),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "Profile not found");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
