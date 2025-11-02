package com.melvstein.ecommerce.api.domain.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Builder
@Jacksonized
public record ItemDto(
        @NotBlank(message = "SKU is required")
        String sku,

        int quantity,
        Date createdAt,
        Date updatedAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
