package com.melvstein.ecommerce.api.domain.user.enums;

import lombok.Getter;

@Getter
public enum UserResponseCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),
    INVALID_ROLE("INVALID_ROLE", "Invalid role"),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "User already exists"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "Username already exists"),
    USER_UNAUTHORIZED("USER_UNAUTHORIZED", "User unauthorized"),
    USER_ACCESS_DENIED("USER_ACCESS_DENIED", "User access denied"),
    REFRESH_TOKEN_EXPIRED("REFRESH_TOKEN_EXPIRED", "Refresh token expired"),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Invalid refresh token"),
    INVALID_PASSWORD("INVALID_PASSWORD", "Invalid password"),
    FILE_UPLOAD_ERROR("FILE_UPLOAD_ERROR", "File upload error");

    private final String code;
    private final String message;

    UserResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
