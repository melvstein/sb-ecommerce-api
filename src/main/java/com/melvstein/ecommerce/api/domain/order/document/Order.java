package com.melvstein.ecommerce.api.domain.order.document;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Jacksonized
public class Order {
    @Id
    String id;

    String customerId;

    String paymentMethod; // e.g., Credit Card, PayPal, etc.
    List<Item> items;

    @Builder.Default
    int status = 0; // 0: Pending, 1: Processing, 2: Shipped, 3: Delivered, 4: Cancelled

    @Builder.Default
    double totalAmount = 0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
