package com.melvstein.ecommerce.api.domain.order.document;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingAddress {
    private String addressType; // e.g., "home", "work", "other"
    private String street;
    private String district;
    private String city;
    private String province;
    private String country;
    private Integer zipCode;
}
