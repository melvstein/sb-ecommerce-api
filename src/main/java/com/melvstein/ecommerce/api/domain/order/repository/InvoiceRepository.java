package com.melvstein.ecommerce.api.domain.order.repository;

import com.melvstein.ecommerce.api.domain.order.document.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    Optional<Invoice> findTopByOrderByInvoiceNumberDesc();
    Optional<Invoice> findByOrderId(String orderId);
}
