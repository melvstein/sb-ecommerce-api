package com.melvstein.ecommerce.api.domain.cart.repository;

import com.melvstein.ecommerce.api.domain.cart.document.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
    public void deleteByCustomerId(String customerId);
    public Optional<Cart> findByCustomerId(String customerId);
}
