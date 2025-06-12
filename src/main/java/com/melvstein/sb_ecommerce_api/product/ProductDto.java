package com.melvstein.sb_ecommerce_api.product;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Builder
@Jacksonized
public record ProductDto(
        String id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        int stock,
        List<String> tags, List<String> images,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
