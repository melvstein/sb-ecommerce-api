package com.melvstein.ecommerce.api.domain.auth.refreshtoken.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token is required")
        String refreshToken,

        @NotBlank(message = "User ID is required")
        String userId
) {
}
