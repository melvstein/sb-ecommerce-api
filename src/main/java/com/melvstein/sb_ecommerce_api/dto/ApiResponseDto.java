package com.melvstein.sb_ecommerce_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ApiResponseDto<T> {
    private String message;
    private T data;
}
