package com.melvstein.sb_ecommerce_api.user;

import java.util.Arrays;

public enum Role {
    ADMIN,
    MANAGER,
    STAFF;

    public static boolean isValid(String role) {
        return Arrays.stream(Role.values())
                .anyMatch(r -> r.name().equalsIgnoreCase(role));
    }
}
