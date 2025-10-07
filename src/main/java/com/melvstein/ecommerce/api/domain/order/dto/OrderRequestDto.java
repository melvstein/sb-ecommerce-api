package com.melvstein.ecommerce.api.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.util.Date;
import java.util.List;

@Builder
public record OrderRequestDto(
        @NotBlank(message = "CustomerId is required")
        String customerId,

        @NotBlank(message = "Payment method is required")
        String paymentMethod,

        @NotEmpty(message = "Items are required")
        List<ItemDto> items,

        int status,

        @PositiveOrZero
        double totalAmount,

        Date createdAt,
        Date updatedAt
) {
}
