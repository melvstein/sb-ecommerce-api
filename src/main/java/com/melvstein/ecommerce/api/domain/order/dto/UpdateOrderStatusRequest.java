package com.melvstein.ecommerce.api.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UpdateOrderStatusRequest(
        @NotBlank(message = "OrderId is required")
        String orderId,

        @Min(value = 0, message = "Status is required")
        int status
) {
}
