package com.melvstein.ecommerce.api.domain.product.mapper;

import com.melvstein.ecommerce.api.domain.product.dto.ProductRequestDto;
import com.melvstein.ecommerce.api.domain.product.document.Product;
import com.melvstein.ecommerce.api.domain.product.dto.ProductDto;
import com.melvstein.ecommerce.api.shared.util.Utils;

import java.util.List;

public final class ProductMapper {

    private ProductMapper() {}

    public static ProductDto toDto(Product product) {
        if (product == null) return null;

        return ProductDto.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .tags(product.getTags())
                .images(product.getImages())
                .createdAt(Utils.fromInstantToDate(product.getUpdatedAt()))
                .updatedAt(Utils.fromInstantToDate(product.getCreatedAt()))
                .build();
    }

    public static List<ProductDto> toDtos(List<Product> products) {
        if (products == null) return null;

        return products.stream().map(ProductMapper::toDto).toList();
    }

    public static Product toDocument(ProductDto productDto) {
        if (productDto == null) return null;

        return Product.builder()
                .id(productDto.id())
                .sku(productDto.sku())
                .name(productDto.name())
                .description(productDto.description())
                .price(productDto.price())
                .stock(productDto.stock())
                .tags(productDto.tags())
                .images(productDto.images())
                .createdAt(productDto.createdAt().toInstant())
                .updatedAt(productDto.updatedAt().toInstant())
                .build();
    }

    public static Product toDocument(ProductRequestDto request) {
        if (request == null) return null;

        return Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .tags(request.tags())
                .images(request.images())
                .build();
    }

    public static List<Product> toDocuments(List<ProductDto> dtos) {
        if (dtos == null) return null;

        return dtos.stream().map(ProductMapper::toDocument).toList();
    }
}
