package com.melvstein.ecommerce.api.domain.customer.document;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class Address {
    private String street;
    private String city;
    private String zipCode;
    private String state;
    private String country;
    private String addressType; // e.g., "home", "work", "other"

    @Builder.Default
    private boolean isDefault = true; // Indicates if this is the default address for the customer
}
