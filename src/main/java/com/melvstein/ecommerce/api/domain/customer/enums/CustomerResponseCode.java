package com.melvstein.ecommerce.api.domain.customer.enums;

import lombok.Getter;

@Getter
public enum CustomerResponseCode {
    CUSTOMER_NOT_FOUND("CUSTOMER_NOT_FOUND", "Customer not found"),
    CUSTOMER_ALREADY_EXISTS("CUSTOMER_ALREADY_EXISTS", "Customer already exists"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "Username already exists"),
    CUSTOMER_UNAUTHORIZED("CUSTOMER_UNAUTHORIZED", "Customer unauthorized"),
    CUSTOMER_ACCESS_DENIED("CUSTOMER_ACCESS_DENIED", "Customer access denied"),
    REFRESH_TOKEN_EXPIRED("REFRESH_TOKEN_EXPIRED", "Refresh token expired"),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Invalid refresh token"),
    INVALID_PASSWORD("INVALID_PASSWORD", "Invalid password");

    private final String code;
    private final String message;

    CustomerResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
