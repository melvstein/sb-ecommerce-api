package com.melvstein.sb_ecommerce_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
}
