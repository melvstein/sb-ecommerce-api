package com.melvstein.ecommerce.api.domain.order.controller;

import com.melvstein.ecommerce.api.domain.cart.document.Cart;
import com.melvstein.ecommerce.api.domain.cart.service.CartService;
import com.melvstein.ecommerce.api.domain.customer.enums.CustomerResponseCode;
import com.melvstein.ecommerce.api.domain.order.document.Invoice;
import com.melvstein.ecommerce.api.domain.order.document.Order;
import com.melvstein.ecommerce.api.domain.order.document.OrderCounter;
import com.melvstein.ecommerce.api.domain.order.document.Receipt;
import com.melvstein.ecommerce.api.domain.order.dto.OrderDto;
import com.melvstein.ecommerce.api.domain.order.dto.OrderRequestDto;
import com.melvstein.ecommerce.api.domain.order.dto.UpdateOrderStatusRequest;
import com.melvstein.ecommerce.api.domain.order.mapper.OrderMapper;
import com.melvstein.ecommerce.api.domain.order.service.InvoiceService;
import com.melvstein.ecommerce.api.domain.order.service.OrderService;
import com.melvstein.ecommerce.api.domain.order.service.ReceiptService;
import com.melvstein.ecommerce.api.domain.product.document.Product;
import com.melvstein.ecommerce.api.domain.product.service.ProductService;
import com.melvstein.ecommerce.api.shared.dto.ApiResponse;
import com.melvstein.ecommerce.api.shared.exception.ApiException;
import com.melvstein.ecommerce.api.shared.util.ApiResponseCode;
import com.melvstein.ecommerce.api.shared.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;
    private final ProductService productService;
    private final InvoiceService invoiceService;
    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> saveOrder(@RequestBody @Valid OrderRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<OrderDto> response = ApiResponse.<OrderDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to create order")
                .data(null)
                .build();

        try {
            Cart cart = cartService.getCartByCustomerId(request.customerId())
                    .orElseThrow(() -> new ApiException(
                            ApiResponseCode.NOT_FOUND.getCode(),
                            "Cart not found",
                            HttpStatus.NOT_FOUND
                    ));

            if (cart.getItems().isEmpty()) {
                throw new ApiException(
                        ApiResponseCode.NOT_FOUND.getCode(),
                        "Empty cart. Cannot create order",
                        HttpStatus.NOT_FOUND
                );
            }

            BigDecimal totalAmount = cart.getItems().stream()
                    .map(item -> {
                        String sku = item.getSku();
                        int quantity = item.getQuantity();

                        Product product = productService.fetchProductBySku(sku)
                                .orElseThrow(() -> new ApiException(
                                        ApiResponseCode.NOT_FOUND.getCode(),
                                        "Product not found. Invalid SKU",
                                        HttpStatus.NOT_FOUND
                                ));

                        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            OrderCounter orderCounter = orderService.getOrderCounter()
                    .orElseThrow(() -> new ApiException(
                            ApiResponseCode.NOT_FOUND.getCode(),
                            "Order counter not found",
                            HttpStatus.NOT_FOUND
                    ));

            Order order = Order.builder()
                    .orderNumber(orderCounter.getLastOrderNumber() + 1)
                    .customerId(cart.getCustomerId())
                    .paymentMethod(request.paymentMethod())
                    .items(cart.getItems())
                    .status(orderService.STATUS_PENDING)
                    .totalAmount(totalAmount)
                    .build();

            Order savedOrder = orderService.saveOrder(order);

            // save last order number
            orderService.saveOrderCounter(OrderCounter.builder()
                            .id(orderCounter.getId())
                            .sequenceName(orderCounter.getSequenceName())
                            .lastOrderNumber(savedOrder.getOrderNumber())
                    .build());

            cartService.deleteCartByCustomerId(request.customerId());

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Order created successfully");
            response.setData(OrderMapper.toDto(savedOrder));

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrdersByCustomerId(@PathVariable String customerId) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<List<OrderDto>> response = ApiResponse.<List<OrderDto>>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to retrieve orders")
                .data(null)
                .build();

        try {
            List<Order> orders = orderService.getOrdersByCustomerId(customerId);

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Order retrieved successfully");
            response.setData(OrderMapper.toDto(orders));

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable String orderId) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<OrderDto> response = ApiResponse.<OrderDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to retrieve order")
                .data(null)
                .build();

        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);

            if (orderOpt.isPresent()) {
                response.setCode(ApiResponseCode.SUCCESS.getCode());
                response.setMessage("Order retrieved successfully");
                response.setData(OrderMapper.toDto(orderOpt.get()));

                return ResponseEntity.ok(response);
            } else {
                response.setCode(CustomerResponseCode.CUSTOMER_NOT_FOUND.getCode());
                response.setMessage("Order not found with id: " + orderId);
                httpStatus = HttpStatus.NOT_FOUND;
            }
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @PostMapping("update-status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(@RequestBody @Valid UpdateOrderStatusRequest request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<OrderDto> response = ApiResponse.<OrderDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to update order status")
                .data(null)
                .build();

        try {
            Optional<Order> orderOpt = orderService.getOrderById(request.orderId());

            if (orderOpt.isPresent()) {
                Order updatedOrder = orderService.updateOrderStatus(request.orderId(), request.status());

                /*List<Integer> invoiceOrderStatuses = List.of(
                        orderService.STATUS_PROCESSING,
                        orderService.STATUS_SHIPPED
                );

                if (invoiceOrderStatuses.contains(request.status())) {
                    BigInteger invoiceNumber = invoiceService.getLastInvoiceNumber()
                            .orElse(BigInteger.ZERO);

                    Invoice invoice = Invoice.builder()
                            .invoiceNumber(invoiceNumber.add(BigInteger.valueOf(1)))
                            .orderId(updatedOrder.getId())
                            .customerId(updatedOrder.getCustomerId())
                            .build();
                    invoiceService.save(invoice);
                }*/

                /*List<Integer> receiptOrderStatuses = List.of(
                        orderService.STATUS_DELIVERED
                );

                if (receiptOrderStatuses.contains(request.status())) {
                    Invoice existingInvoice = invoiceService.getInvoiceByOrderId(updatedOrder.getId())
                            .orElseThrow(() -> new ApiException(
                                    ApiResponseCode.NOT_FOUND.getCode(),
                                    "Invoice not found for order id: " + updatedOrder.getId(),
                                    HttpStatus.NOT_FOUND
                            ));

                    Receipt receipt = Receipt.builder()
                            .orderId(updatedOrder.getId())
                            .customerId(updatedOrder.getCustomerId())
                            .receiptNumber(existingInvoice.getInvoiceNumber())
                            .build();

                    receiptService.save(receipt);
                }*/

                response.setCode(ApiResponseCode.SUCCESS.getCode());
                response.setMessage("Order status updated successfully");
                response.setData(OrderMapper.toDto(updatedOrder));

                return ResponseEntity.ok(response);
            } else {
                response.setCode(CustomerResponseCode.CUSTOMER_NOT_FOUND.getCode());
                response.setMessage("Order not found with id: " + request.orderId());
                httpStatus = HttpStatus.NOT_FOUND;
            }
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDto>> deleteOrderById(@PathVariable String orderId) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<OrderDto> response = ApiResponse.<OrderDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to delete order")
                .data(null)
                .build();

        try {
            Order order = orderService
                    .getOrderById(orderId)
                    .orElseThrow(() -> new ApiException(
                            ApiResponseCode.NOT_FOUND.getCode(),
                            ApiResponseCode.NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            orderService.removeOrder(orderId);
            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Order deleted successfully");
            response.setData(OrderMapper.toDto(order));

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }
}
