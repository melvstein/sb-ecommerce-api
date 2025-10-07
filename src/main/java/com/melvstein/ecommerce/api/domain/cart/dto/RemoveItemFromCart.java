package com.melvstein.ecommerce.api.domain.cart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

@Builder
@Jacksonized
public record RemoveItemFromCart(
        @NotBlank(message = "CustomerId is required")
        String customerId,

        @NotBlank(message = "ProductId is required")
        String productId
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
