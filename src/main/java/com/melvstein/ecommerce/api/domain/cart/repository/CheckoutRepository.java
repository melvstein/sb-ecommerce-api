package com.melvstein.ecommerce.api.domain.cart.repository;

import com.melvstein.ecommerce.api.domain.cart.document.Checkout;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckoutRepository extends MongoRepository<Checkout, String> {
}
