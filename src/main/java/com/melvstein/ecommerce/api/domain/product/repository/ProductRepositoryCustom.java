package com.melvstein.ecommerce.api.domain.product.repository;

import com.melvstein.ecommerce.api.domain.product.document.Product;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> filter(List<String> filter, Pageable pageable);
}
