package com.melvstein.ecommerce.api.domain.cart.mapper;

import com.melvstein.ecommerce.api.domain.cart.document.Cart;
import com.melvstein.ecommerce.api.domain.cart.dto.AddToCartRequestDto;
import com.melvstein.ecommerce.api.domain.cart.dto.CartDto;
import com.melvstein.ecommerce.api.shared.util.Utils;

public class CartMapper {
    public static CartDto toDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        return CartDto.builder()
                .id(cart.getId())
                .customerId(cart.getCustomerId())
                .items(ItemMapper.toDto(cart.getItems()))
                .createdAt(Utils.fromInstantToDate(cart.getCreatedAt()))
                .updatedAt(Utils.fromInstantToDate(cart.getUpdatedAt()))
                .build();
    }

    public static Cart toDocument(CartDto cartDto) {
        if (cartDto == null) {
            return null;
        }

        return Cart.builder()
                .id(cartDto.id())
                .customerId(cartDto.customerId())
                .items(ItemMapper.toDocument(cartDto.items()))
                .createdAt(cartDto.createdAt().toInstant())
                .updatedAt(cartDto.updatedAt().toInstant())
                .build();
    }

    public static Cart toDocument(AddToCartRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }

        return Cart.builder()
                .customerId(requestDto.CustomerId())
                .items(ItemMapper.toDocument(requestDto.items()))
                .build();
    }
}
