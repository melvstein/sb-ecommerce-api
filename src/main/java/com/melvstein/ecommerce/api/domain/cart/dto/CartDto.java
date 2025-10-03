package com.melvstein.ecommerce.api.domain.cart.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Builder
@Jacksonized
public record CartDto(
        String id,

        String customerId,
        List<ItemDto> items,
        Date updatedAt,
        Date createdAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
