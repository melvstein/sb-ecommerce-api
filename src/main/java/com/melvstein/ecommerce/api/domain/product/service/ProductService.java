package com.melvstein.ecommerce.api.domain.product.service;

import com.melvstein.ecommerce.api.domain.product.document.Product;
import com.melvstein.ecommerce.api.domain.product.dto.ProductDto;
import com.melvstein.ecommerce.api.domain.product.mapper.ProductMapper;
import com.melvstein.ecommerce.api.domain.product.repository.ProductRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final PagedResourcesAssembler<Product> productPagedResourcesAssembler;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public PagedModel<EntityModel<Product>> getAllProducts(@Nullable List<String> filter, Pageable pageable) {
        List<Product> filteredProducts = productRepository.filter(filter, pageable);
        Page<Product> productPage = new PageImpl<>(filteredProducts, pageable, filteredProducts.size());
        PagedModel<EntityModel<Product>> productPagedModel = productPagedResourcesAssembler.toModel(productPage);

        List<EntityModel<Product>> productDtoContent = productPagedModel.getContent().stream()
                .map(entityModel -> {
                    assert entityModel.getContent() != null;
                    return EntityModel.of(entityModel.getContent());
                })
                .toList();

        return PagedModel.of(
                productDtoContent,
                productPagedModel.getMetadata(),
                productPagedModel.getLinks()
        );
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> fetchProductById(String id) {
        return productRepository.findById(id);
    }

    public Optional<Product> fetchProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    public boolean existsById(String id) {
        return productRepository.existsById(id);
    }

    public void deleteProductById(String id) {
        productRepository.deleteById(id);
    }

    public void deleteProductBySku(String sku) {
        productRepository.deleteBySku(sku);
    }
}
