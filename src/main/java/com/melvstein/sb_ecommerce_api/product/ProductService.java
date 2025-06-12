package com.melvstein.sb_ecommerce_api.product;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    private PagedResourcesAssembler<Product> pagedResourcesAssembler;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public PagedModel<EntityModel<Product>> getAllProducts(@Nullable List<String> filter, Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        return pagedResourcesAssembler.toModel(page);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }
}
