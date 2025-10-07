package com.melvstein.ecommerce.api.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UpdateOrderStatusRequest(
        @NotBlank(message = "OrderId is required")
        String orderId,

        @NotBlank(message = "Status is required")
        int status
) {
}
