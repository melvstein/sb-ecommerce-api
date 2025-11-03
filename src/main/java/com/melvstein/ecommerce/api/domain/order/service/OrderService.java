package com.melvstein.ecommerce.api.domain.order.service;

import com.melvstein.ecommerce.api.domain.order.document.Order;
import com.melvstein.ecommerce.api.domain.order.document.OrderCounter;
import com.melvstein.ecommerce.api.domain.order.repository.OrderCounterRepository;
import com.melvstein.ecommerce.api.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderCounterRepository orderCounterRepository;

    public final String ORDER_PREFIX = "ORD";
    public final String INVOICE_PREFIX = "INV";
    public final String RECEIPT_PREFIX = "RCPT";
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

    public List<Order> getOrdersByCustomerIdAndStatus(String customerId, int status) {
        return orderRepository.findAllByCustomerIdAndStatus(customerId, status);
    }

    public List<Order> getOrdersByCustomerIdAndStatusNot(String customerId, int status) {
        return orderRepository.findAllByCustomerIdAndStatusNot(customerId, status);
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

    public Optional<OrderCounter> getOrderCounter() {
        OrderCounter orderCounter = orderCounterRepository.findBySequenceName("orderNumber").orElse(
                OrderCounter.builder()
                        .sequenceName("orderNumber")
                        .lastOrderNumber(0)
                        .build()
        );

        return Optional.ofNullable(orderCounter);
    }

    public OrderCounter saveOrderCounter(OrderCounter orderCounter) {
        return orderCounterRepository.save(orderCounter);
    }

    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    public String generateInvoiceNumber(long orderNumber) {
        int year = Year.now().getValue();
        String formatted = String.format("%d-%04d", year, orderNumber);
        return INVOICE_PREFIX + "-" + formatted;
    }

    public String generateReceiptNumber(long orderNumber) {
        int year = Year.now().getValue();
        String formatted = String.format("%d-%04d", year, orderNumber);
        return RECEIPT_PREFIX + "-" + formatted;
    }
}
