package com.melvstein.sb_ecommerce_api.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "Role is required")
        String role,

        @Email(message = "Invalid email address")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}
