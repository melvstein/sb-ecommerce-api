package com.melvstein.ecommerce.api.domain.security.authentication.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record LoginResponseDto(
        String accessToken,
        String refreshToken
) {
}
