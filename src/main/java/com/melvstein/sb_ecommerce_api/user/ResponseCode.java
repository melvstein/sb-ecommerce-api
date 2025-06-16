package com.melvstein.sb_ecommerce_api.user;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS("SUCCESS", "Success"),
    GENERAL_ERROR("GENERAL_ERROR", "General Error"),
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),
    INVALID_ROLE("INVALID_ROLE", "Invalid Role");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
