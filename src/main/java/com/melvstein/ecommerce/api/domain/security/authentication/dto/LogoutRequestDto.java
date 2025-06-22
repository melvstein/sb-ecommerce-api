package com.melvstein.ecommerce.api.domain.security.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record LogoutRequestDto(
        @NotBlank(message = "User ID is required")
        String userId
) {
}
