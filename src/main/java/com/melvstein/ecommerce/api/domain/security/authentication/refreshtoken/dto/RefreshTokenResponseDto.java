package com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record RefreshTokenResponseDto(
        String accessToken,
        String refreshToken
) {
}
