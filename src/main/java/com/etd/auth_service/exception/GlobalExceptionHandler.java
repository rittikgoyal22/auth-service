package com.etd.auth_service.exception;

import com.etd.auth_service.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> handleBadRequestException(BadRequestException ex) {
        ErrorDTO errorDTO = ErrorDTO.builder()
                .message(ex.getMessage())
                .fieldName(ex.getFieldName())
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return new ResponseEntity<>(errorDTO, errorDTO.getStatus());
    }

}
