package com.melvstein.sb_ecommerce_api.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class Product {
    @Id
    private String id;

    @Indexed(unique = true)
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private List<String> tags;
    private List<String> images;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Date getDateCreatedAt() {
       if (createdAt == null) return null;

        return Date.from(createdAt);
    }

    public Date getDateUpdatedAt() {
        if (updatedAt == null) return null;

        return Date.from(updatedAt);
    }
}
