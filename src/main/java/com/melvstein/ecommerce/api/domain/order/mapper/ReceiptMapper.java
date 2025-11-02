package com.melvstein.ecommerce.api.domain.order.mapper;

import com.melvstein.ecommerce.api.domain.order.document.Receipt;
import com.melvstein.ecommerce.api.domain.order.dto.ReceiptDto;
import com.melvstein.ecommerce.api.shared.util.Utils;

public class ReceiptMapper {
    public static ReceiptDto toDto(Receipt receipt) {
        if (receipt == null) {
            return null;
        }

        return ReceiptDto.builder()
                .receiptNumber(receipt.getReceiptNumber())
                .transactionId(receipt.getTransactionId())
                .remarks(receipt.getRemarks())
                .refundedAt(Utils.fromInstantToDate(receipt.getRefundedAt()))
                .createdAt(Utils.fromInstantToDate(receipt.getCreatedAt()))
                .updatedAt(Utils.fromInstantToDate(receipt.getUpdatedAt()))
                .build();
    }

    public static Receipt toDocument(ReceiptDto dto) {
        if (dto == null) {
            return null;
        }

        return Receipt.builder()
                .receiptNumber(dto.receiptNumber())
                .transactionId(dto.transactionId())
                .remarks(dto.remarks())
                .refundedAt(dto.refundedAt().toInstant())
                .createdAt(dto.createdAt() != null ? dto.createdAt().toInstant() : null)
                .updatedAt(dto.updatedAt() != null ? dto.updatedAt().toInstant() : null)
                .build();
    }
}
