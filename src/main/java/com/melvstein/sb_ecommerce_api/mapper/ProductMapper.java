package com.melvstein.sb_ecommerce_api.mapper;

import com.melvstein.sb_ecommerce_api.dto.ProductDto;
import com.melvstein.sb_ecommerce_api.model.Product;

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
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static List<ProductDto> toDtos(List<Product> products) {
        if (products == null) return null;

        return products.stream().map(ProductMapper::toDto).toList();
    }

    public static Product toDocument(ProductDto dto) {
        if (dto == null) return null;

        return Product.builder()
                .id(dto.getId())
                .sku(dto.getSku())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .tags(dto.getTags())
                .images(dto.getImages())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public static List<Product> toDocuments(List<ProductDto> dtos) {
        if (dtos == null) return null;

        return dtos.stream().map(ProductMapper::toDocument).toList();
    }
}
