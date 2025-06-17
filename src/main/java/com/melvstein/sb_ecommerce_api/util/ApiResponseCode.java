package com.melvstein.sb_ecommerce_api.util;

import lombok.Getter;

@Getter
public enum ApiResponseCode {
    SUCCESS("SUCCESS", "Success"),
    ERROR("ERROR", "Error");

    private final String code;
    private final String message;

    ApiResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
