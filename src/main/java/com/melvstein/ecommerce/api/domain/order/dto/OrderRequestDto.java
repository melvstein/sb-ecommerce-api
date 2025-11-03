package com.melvstein.ecommerce.api.domain.order.dto;

import com.melvstein.ecommerce.api.domain.order.document.ShippingDetails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

        @NotNull(message = "Shipping details are required")
        ShippingDetails shippingDetails
) {
}
