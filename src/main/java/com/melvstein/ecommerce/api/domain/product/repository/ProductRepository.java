package com.melvstein.ecommerce.api.domain.product.repository;

import com.melvstein.ecommerce.api.domain.product.document.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findBySku(String sku);
    void deleteBySku(String sku);
}
