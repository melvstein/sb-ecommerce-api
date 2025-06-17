package com.melvstein.sb_ecommerce_api.user;

import lombok.Getter;

@Getter
public enum UserResponseCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),
    INVALID_ROLE("INVALID_ROLE", "Invalid role"),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "User already exists"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "Username already exists"),
    USER_UNAUTHORIZED("USER_UNAUTHORIZED", "User unauthorized"),
    USER_ACCESS_DENIED("USER_ACCESS_DENIED", "User access denied");

    private final String code;
    private final String message;

    UserResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
