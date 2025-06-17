package com.melvstein.sb_ecommerce_api.product;

import lombok.Getter;

@Getter
public enum ProductResponseCode {
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Product not found");

    private final String code;
    private final String message;

    ProductResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
