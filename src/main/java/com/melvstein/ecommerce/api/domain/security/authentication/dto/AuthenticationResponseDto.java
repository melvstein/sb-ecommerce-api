package com.melvstein.ecommerce.api.domain.security.authentication.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record AuthenticationResponseDto(
        String accessToken,
        String refreshToken
) {
}
