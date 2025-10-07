package com.melvstein.ecommerce.api.domain.cart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Builder
@Jacksonized
public record MinusToCartRequestDto(
        @NotBlank(message = "CustomerId is required")
        String CustomerId,

        @NotEmpty(message = "Items are required")
        List<ItemDto> items
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
