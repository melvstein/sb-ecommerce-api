package com.melvstein.ecommerce.api.domain.order.document;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Jacksonized
public class Receipt {
    @Indexed(unique = true)
    private String receiptNumber;

    private String remarks;
    private String transactionId;
    private Instant refundedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
