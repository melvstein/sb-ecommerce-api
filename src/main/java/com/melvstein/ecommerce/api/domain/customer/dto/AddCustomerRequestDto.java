package com.melvstein.ecommerce.api.domain.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddCustomerRequestDto(
        @NotBlank(message = "Required parameter 'provider'")
        String provider,

        @NotBlank(message = "Required parameter 'provider'")
        String username,

        @Email
        @NotBlank(message = "Required parameter 'email'")
        String email,

        @NotBlank(message = "Required parameter 'firstName'")
        String firstName,

        @NotBlank(message = "Required parameter 'middleName'")
        String middleName,

        @NotBlank(message = "Required parameter 'lastName'")
        String lastName,

        @NotBlank(message = "Required parameter 'contactNumber'")
        String contactNumber,

        AddressDto address
) {
}
