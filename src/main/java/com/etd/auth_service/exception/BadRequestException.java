package com.etd.auth_service.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {

    private final String fieldName;

    public BadRequestException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

}
