package com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.repository;

public interface RefreshTokenRepositoryCustom {
    boolean isValid(String token);
}
