package com.melvstein.ecommerce.api.domain.order.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Date;

@Builder
@Jacksonized
public record InvoiceDto(
        String invoiceNumber,
        Date createdAt,
        Date updatedAt
) {
}
