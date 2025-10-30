package com.melvstein.ecommerce.api.domain.order.repository;

import com.melvstein.ecommerce.api.domain.order.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    public List<Order> findAllByCustomerId(String customerId);
}
