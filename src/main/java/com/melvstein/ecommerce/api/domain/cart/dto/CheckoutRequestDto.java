package com.melvstein.ecommerce.api.domain.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record CheckoutRequestDto(
        @NotBlank(message = "Cart ID is required")
        String cartId
) {
}
