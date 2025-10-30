package com.melvstein.ecommerce.api.domain.order.document;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "order_counters")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Jacksonized
public class OrderCounter {
    @Id
    private String id;

    @Indexed(unique = true)
    private String sequenceName;

    @Builder.Default
    private long lastOrderNumber = 0;
}
