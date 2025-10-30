package com.melvstein.ecommerce.api.domain.order.repository;

import com.melvstein.ecommerce.api.domain.order.document.OrderCounter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrderCounterRepository extends MongoRepository<OrderCounter, String> {
    Optional<OrderCounter> findBySequenceName(String sequenceName);
}
