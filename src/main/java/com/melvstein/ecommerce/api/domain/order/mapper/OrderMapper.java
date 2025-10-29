package com.melvstein.ecommerce.api.domain.order.mapper;

import com.melvstein.ecommerce.api.domain.order.document.Order;
import com.melvstein.ecommerce.api.domain.order.dto.OrderDto;
import com.melvstein.ecommerce.api.domain.order.dto.OrderRequestDto;
import com.melvstein.ecommerce.api.shared.util.Utils;
import com.melvstein.ecommerce.api.domain.cart.mapper.ItemMapper;

import java.time.Instant;

public class OrderMapper {

    public static OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }

        return OrderDto.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .paymentMethod(order.getPaymentMethod())
                .items(ItemMapper.toDto(order.getItems()))
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt() != null ? Utils.fromInstantToDate(order.getCreatedAt()) : Utils.fromInstantToDate(Instant.now()))
                .updatedAt(order.getUpdatedAt() != null ? Utils.fromInstantToDate(order.getUpdatedAt()) : Utils.fromInstantToDate(Instant.now()))
                .build();
    }

    public static Order toDocument(OrderDto orderDto) {
        if (orderDto == null) {
            return null;
        }

        Instant createdAt = orderDto.createdAt() != null ? orderDto.createdAt().toInstant() : Instant.now();
        Instant updatedAt = orderDto.updatedAt() != null ? orderDto.updatedAt().toInstant() : Instant.now();

        return Order.builder()
                .id(orderDto.id())
                .customerId(orderDto.customerId())
                .paymentMethod(orderDto.paymentMethod())
                .items(ItemMapper.toDocument(orderDto.items()))
                .status(orderDto.status())
                .totalAmount(orderDto.totalAmount())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static Order toDocument(OrderRequestDto orderDto) {
        if (orderDto == null) {
            return null;
        }

        return Order.builder()
                .customerId(orderDto.customerId())
                .paymentMethod(orderDto.paymentMethod())
                .build();
    }
}
