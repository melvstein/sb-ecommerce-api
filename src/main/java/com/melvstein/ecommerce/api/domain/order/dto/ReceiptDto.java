package com.melvstein.ecommerce.api.domain.order.dto;

import lombok.Builder;

import java.util.Date;

@Builder
public record ReceiptDto(
        String receiptNumber,
        String remarks,
        String transactionId,
        Date refundedAt,
        Date createdAt,
        Date updatedAt
) {
}
