package com.melvstein.ecommerce.api.domain.security.authentication.usertoken.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Date;

@Builder
@Jacksonized
public record UserTokenDto(
        String id,
        String token,
        String userId,
        String type,
        long timeout,
        boolean isAvailable,
        Date expiredAt,
        Date createdAt,
        Date updatedAt
) {
}
