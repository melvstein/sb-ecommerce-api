package com.melvstein.ecommerce.api.domain.order.repository;

import com.melvstein.ecommerce.api.domain.order.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
}
