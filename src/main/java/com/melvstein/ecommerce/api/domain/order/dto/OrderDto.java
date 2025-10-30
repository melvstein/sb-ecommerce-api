package com.melvstein.ecommerce.api.domain.order.dto;

import com.melvstein.ecommerce.api.domain.cart.dto.ItemDto;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Builder
@Jacksonized
public record OrderDto(
        String id,
        long orderNumber,
        String customerId,
        String paymentMethod,
        List<ItemDto> items,
        int status,
        BigDecimal totalAmount,
        Date createdAt,
        Date updatedAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
