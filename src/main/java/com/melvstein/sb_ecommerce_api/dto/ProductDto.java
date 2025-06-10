package com.melvstein.sb_ecommerce_api.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@Getter
@ToString
@AllArgsConstructor
@Jacksonized
public class ProductDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String sku;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final int stock;
    private final List<String> tags;
    private final List<String> images;
    private final Instant createdAt;
    private final Instant updatedAt;
}
