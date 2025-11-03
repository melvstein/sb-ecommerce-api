package com.melvstein.ecommerce.api.domain.order.document;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingDetails {
    private String receiverFirstName;
    private String receiverMiddleName;
    private String receiverLastName;
    private String receiverContactNumber;
    private ShippingAddress shippingAddress;

    @Builder.Default
    private boolean isDefault = false;
}
