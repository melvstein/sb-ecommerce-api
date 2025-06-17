package com.melvstein.sb_ecommerce_api.user;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Builder
@Jacksonized
public record UserDto(
        String id,
        String role,
        String email,
        String username,
        String profileImageUrl,
        boolean isActive,
        boolean isVerified,
        Date lastLoginAt,
        Date createdAt,
        Date updatedAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
