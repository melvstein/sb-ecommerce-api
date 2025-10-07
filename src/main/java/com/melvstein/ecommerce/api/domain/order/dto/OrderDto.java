package com.melvstein.ecommerce.api.domain.order.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Builder
@Jacksonized
public record OrderDto(
        String id,
        String customerId,
        String paymentMethod,
        List<ItemDto> items,
        int status,
        double totalAmount,
        Date createdAt,
        Date updatedAt
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
