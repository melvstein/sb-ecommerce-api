package com.melvstein.ecommerce.api.shared.util;

import lombok.Getter;

@Getter
public enum ApiResponseCode {
    SUCCESS("SUCCESS", "Success"),
    ERROR("ERROR", "Error"),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized");

    private final String code;
    private final String message;

    ApiResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
