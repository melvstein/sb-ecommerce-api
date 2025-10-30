package com.melvstein.ecommerce.api.domain.order.repository;

import com.melvstein.ecommerce.api.domain.order.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findAllByCustomerId(String customerId);
    Optional<Order> findTopByOrderByOrderNumberDesc();
}
