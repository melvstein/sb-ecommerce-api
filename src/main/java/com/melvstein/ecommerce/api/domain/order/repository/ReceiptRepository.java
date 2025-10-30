package com.melvstein.ecommerce.api.domain.order.repository;

import com.melvstein.ecommerce.api.domain.order.document.Receipt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptRepository extends MongoRepository<Receipt, String> {

}
