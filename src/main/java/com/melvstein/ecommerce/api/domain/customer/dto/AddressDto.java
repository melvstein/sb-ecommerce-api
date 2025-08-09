package com.melvstein.ecommerce.api.domain.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record AddressDto(
        @NotBlank(message = "Address type is required")
        String addressType,

        @NotBlank(message = "Street is required")
        String street,

        @NotBlank(message = "District is required")
        String district,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Province is required")
        String province,

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "Zip code is required")
        Integer zipCode) {
}
