package com.melvstein.ecommerce.api.domain.order.mapper;

import com.melvstein.ecommerce.api.domain.order.document.Invoice;
import com.melvstein.ecommerce.api.domain.order.dto.InvoiceDto;
import com.melvstein.ecommerce.api.shared.util.Utils;

public class InvoiceMapper {
    public static InvoiceDto toDto(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        return InvoiceDto.builder()
                .invoiceNumber(invoice.getInvoiceNumber())
                .createdAt(Utils.fromInstantToDate(invoice.getCreatedAt()))
                .updatedAt(Utils.fromInstantToDate(invoice.getUpdatedAt()))
                .build();
    }

    public static Invoice toDocument(InvoiceDto dto) {
        if (dto == null) {
            return null;
        }

        return Invoice.builder()
                .invoiceNumber(dto.invoiceNumber())
                .createdAt(dto.createdAt().toInstant())
                .updatedAt(dto.updatedAt().toInstant())
                .build();
    }
}
