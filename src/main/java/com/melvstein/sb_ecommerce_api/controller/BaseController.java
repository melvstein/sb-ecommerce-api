package com.melvstein.sb_ecommerce_api.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.melvstein.sb_ecommerce_api.dto.ApiResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Bad Request");

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.<Map<String, Object>>builder()
                        .message(message)
                        .data(null)
                        .build()
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleDeserializationError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();  // Use getCause() to access JsonMappingException
        String customMessage = "Bad Request";
        String fieldName = "Unknown field";

        if (cause instanceof JsonMappingException jsonMappingException) {
            List<JsonMappingException.Reference> path = jsonMappingException.getPath();
            if (!path.isEmpty()) {
                fieldName = path.getLast().getFieldName();
            }

            if (cause.getMessage().contains("java.math.BigDecimal")) {
                customMessage = String.format("Field '%s' must be a valid number.", fieldName);
            } else {
                customMessage = String.format("Invalid value for field '%s'.", fieldName);
            }
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, Object>>builder()
                        .message(customMessage)
                        .data(null)
                        .build());
    }
}
