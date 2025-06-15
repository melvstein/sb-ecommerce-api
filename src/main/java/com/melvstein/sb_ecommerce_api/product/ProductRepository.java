package com.melvstein.sb_ecommerce_api.product;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    public Optional<Product> findBySku(String sku);
    public void deleteBySku(String sku);
}
