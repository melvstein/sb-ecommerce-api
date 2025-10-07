package com.melvstein.ecommerce.api.domain.order.mapper;

import com.melvstein.ecommerce.api.domain.order.document.Item;
import com.melvstein.ecommerce.api.domain.order.dto.ItemDto;
import com.melvstein.ecommerce.api.shared.util.Utils;

import java.time.Instant;
import java.util.List;

public class ItemMapper {
    public static List<ItemDto> toDto(List<Item> items) {
        if (items == null) {
            return null;
        }

        return items.stream()
                .map(item -> ItemDto.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .createdAt(Utils.fromInstantToDate(item.getCreatedAt()))
                        .updatedAt(Utils.fromInstantToDate(item.getUpdatedAt()))
                        .build()
                )
                .toList();
    }

    public static List<Item> toDocument(List<ItemDto> itemDtos) {
        if (itemDtos == null) {
            return null;
        }

        return itemDtos.stream()
                .map(itemDto -> {
                    Instant createdAt = itemDto.createdAt() != null ? itemDto.createdAt().toInstant() : Instant.now();
                    Instant updatedAt = itemDto.updatedAt() != null ? itemDto.updatedAt().toInstant() : Instant.now();

                    return Item.builder()
                            .productId(itemDto.productId())
                            .quantity(itemDto.quantity())
                            .createdAt(createdAt)
                            .updatedAt(updatedAt)
                            .build();
                }).toList();
    }

    public static Item toDocument(ItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }

        Instant createdAt = itemDto.createdAt() != null ? itemDto.createdAt().toInstant() : Instant.now();
        Instant updatedAt = itemDto.updatedAt() != null ? itemDto.updatedAt().toInstant() : Instant.now();

        return Item.builder()
                .productId(itemDto.productId())
                .quantity(itemDto.quantity())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}