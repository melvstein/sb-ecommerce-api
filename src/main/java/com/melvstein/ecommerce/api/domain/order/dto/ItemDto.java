package com.melvstein.ecommerce.api.domain.order.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.Date;

@Builder
public record ItemDto(
        String productId,
        int quantity,
        Date createdAt,
        Date updatedAt
) {
}
