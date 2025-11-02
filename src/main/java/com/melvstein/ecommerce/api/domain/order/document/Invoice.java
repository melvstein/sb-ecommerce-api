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
public class Invoice {
    @Indexed(unique = true)
    private String invoiceNumber;
    private Instant createdAt;
    private Instant updatedAt;
}
