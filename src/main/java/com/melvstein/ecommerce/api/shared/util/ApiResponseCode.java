package com.melvstein.ecommerce.api.shared.util;

import lombok.Getter;

@Getter
public enum ApiResponseCode {
    SUCCESS("SUCCESS", "Success"),
    ERROR("ERROR", "Error"),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized"),
    FILE_UPLOAD_ERROR("FILE_UPLOAD_ERROR", "File upload error"),
    NOT_FOUND("NOT_FOUND", "Not found"),
    INVALID_REQUEST("INVALID_REQUEST", "Invalid request");

    private final String code;
    private final String message;

    ApiResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
