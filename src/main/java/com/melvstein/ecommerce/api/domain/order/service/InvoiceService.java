package com.melvstein.ecommerce.api.domain.order.service;

import com.melvstein.ecommerce.api.domain.order.document.Invoice;
import com.melvstein.ecommerce.api.domain.order.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
@AllArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public Invoice save(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    public Optional<BigInteger> getLastInvoiceNumber() {
        return invoiceRepository.findTopByOrderByInvoiceNumberDesc()
                .map(Invoice::getInvoiceNumber);
    }

    public Optional<Invoice> getInvoiceByOrderId(String orderId) {
        return invoiceRepository.findByOrderId(orderId);
    }
}
