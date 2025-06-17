package com.melvstein.ecommerce.api.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@AllArgsConstructor
@Builder
@Jacksonized
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
}
