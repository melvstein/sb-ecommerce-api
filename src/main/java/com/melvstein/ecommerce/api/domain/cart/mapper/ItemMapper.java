package com.melvstein.ecommerce.api.domain.cart.mapper;

import com.melvstein.ecommerce.api.domain.cart.document.Item;
import com.melvstein.ecommerce.api.domain.cart.dto.ItemDto;
import com.melvstein.ecommerce.api.shared.util.Utils;

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
                .map(itemDto -> Item.builder()
                        .productId(itemDto.productId())
                        .quantity(itemDto.quantity())
                        .createdAt(itemDto.createdAt().toInstant())
                        .updatedAt(itemDto.updatedAt().toInstant())
                        .build()
                )
                .toList();
    }
}
