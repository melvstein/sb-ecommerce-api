package com.melvstein.sb_ecommerce_api.product;

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
                .id(dto.id())
                .sku(dto.sku())
                .name(dto.name())
                .description(dto.description())
                .price(dto.price())
                .stock(dto.stock())
                .tags(dto.tags())
                .images(dto.images())
                .createdAt(dto.createdAt())
                .updatedAt(dto.updatedAt())
                .build();
    }

    public static Product toDocument(ProductRequest request) {
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
