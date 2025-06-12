package com.melvstein.sb_ecommerce_api.product;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    public Product findBySku(String sku);
}
