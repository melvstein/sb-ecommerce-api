package com.melvstein.ecommerce.api.domain.customer.document;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class Address {
    private String addressType; // e.g., "home", "work", "other"
    private String street;
    private String district;
    private String city;
    private String province;
    private String country;
    private Integer zipCode;

    @Builder.Default
    private boolean isDefault = true; // Indicates if this is the default address for the customer
}
