package com.melvstein.ecommerce.api.domain.auth.refreshtoken.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Date;

@Builder
@Jacksonized
public record RefreshTokenDto(
        String id,
        String token,
        String userId,
        long timeout,
        Date expiredAt,
        Date createdAt,
        Date updatedAt
) {
}
