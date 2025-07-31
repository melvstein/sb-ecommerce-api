package com.melvstein.ecommerce.api.domain.customer.dto;

import com.melvstein.ecommerce.api.domain.customer.document.Address;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Builder
@Jacksonized
public record CustomerDto(
        String id,
        String username,
        String email,
        String firstName,
        String middleName,
        String lastName,
        String contactNumber,
        String provider,
        String profileImageUrl,
        Address address,
        boolean isActive,
        boolean isVerified,
        Date lastLoginAt,
        Date createdAt,
        Date updatedAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
