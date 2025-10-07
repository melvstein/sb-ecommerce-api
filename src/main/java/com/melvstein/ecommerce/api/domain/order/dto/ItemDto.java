package com.melvstein.ecommerce.api.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.Date;

@Builder
public record ItemDto(
        @NotBlank(message = "SKU is required")
        String sku,

        int quantity,
        Date createdAt,
        Date updatedAt
) {
}
