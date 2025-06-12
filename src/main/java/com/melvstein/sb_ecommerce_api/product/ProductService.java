package com.melvstein.sb_ecommerce_api.product;

import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getAllProducts(@Null List<String> filter, Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }
}
