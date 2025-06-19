package com.melvstein.ecommerce.api.domain.auth.refreshtoken.repository;

public interface RefreshTokenRepositoryCustom {
    boolean isValid(String token);
}
