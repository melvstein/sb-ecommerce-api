package com.melvstein.ecommerce.api.domain.cart.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Date;
import java.util.List;

@Builder
@Jacksonized
public record CheckoutDto(
        String id,
        String cartId,
        String customerId,
        List<ItemDto> items,
        Date createdAt,
        Date updatedAt
) {
}
