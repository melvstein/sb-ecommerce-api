package com.melvstein.sb_ecommerce_api.product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank(message = "Required parameter 'sku'")
        String sku,

        @NotBlank(message = "Required parameter 'name'")
        String name,

        @NotBlank(message = "Required parameter 'description'")
        String description,

        @NotNull(message = "Required parameter 'price'")
        @DecimalMin(value = "0.0", message = "Price must be a positive number")
        BigDecimal price,

        @NotNull(message = "Required parameter 'stock'")
        @Min(value = 0)
        int stock,

        @NotEmpty(message = "List of tags cannot be empty")
        List<String> tags, List<String> images
) {
}
