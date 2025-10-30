package com.melvstein.ecommerce.api.domain.order.service;

import com.melvstein.ecommerce.api.domain.order.document.Order;
import com.melvstein.ecommerce.api.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    public final String ORDER_PREFIX = "ORD-";
    public final int STATUS_PENDING = 0;
    public final int STATUS_PROCESSING = 1;
    public final int STATUS_SHIPPED = 2;
    public final int STATUS_DELIVERED = 3;
    public final int STATUS_CANCELLED = 4;

    public Order saveOrder(Order order) {
        // Business logic for placing an order can be added here
        return orderRepository.save(order);
    }

    public void removeOrder(String orderId) {
        orderRepository.deleteById(orderId);
    }

    public Optional<Order> getOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrdersByCustomerId(String customerId) {
        return orderRepository.findAllByCustomerId(customerId);
    }

    public Order updateOrderStatus(String orderId, int status) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        return null;
    }
}
