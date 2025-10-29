package com.melvstein.ecommerce.api.domain.cart.mapper;

import com.melvstein.ecommerce.api.domain.cart.document.Checkout;
import com.melvstein.ecommerce.api.domain.cart.dto.CheckoutDto;
import com.melvstein.ecommerce.api.shared.util.Utils;

public class CheckoutMapper {
    public static CheckoutDto toDto(Checkout checkout) {
        if (checkout == null) {
            return null;
        }

        return CheckoutDto.builder()
                .id(checkout.getId())
                .cartId(checkout.getCartId())
                .customerId(checkout.getCustomerId())
                .items(ItemMapper.toDto(checkout.getItems()))
                .createdAt(Utils.fromInstantToDate(checkout.getCreatedAt()))
                .updatedAt(Utils.fromInstantToDate(checkout.getUpdatedAt()))
                .build();
    }

    public static Checkout toDocument(CheckoutDto dto) {
        if (dto == null) {
            return null;
        }

        return Checkout.builder()
                .id(dto.id())
                .cartId(dto.cartId())
                .customerId(dto.customerId())
                .items(ItemMapper.toDocument(dto.items()))
                .createdAt(dto.createdAt().toInstant())
                .updatedAt(dto.updatedAt().toInstant())
                .build();
    }
}
