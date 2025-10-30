package com.melvstein.ecommerce.api.domain.order.service;

import com.melvstein.ecommerce.api.domain.order.document.Receipt;
import com.melvstein.ecommerce.api.domain.order.repository.ReceiptRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReceiptService {
    private final ReceiptRepository receiptRepository;

    public Receipt save(Receipt receipt) {
        return receiptRepository.save(receipt);
    }
}
