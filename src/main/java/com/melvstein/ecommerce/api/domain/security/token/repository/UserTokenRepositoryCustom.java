package com.melvstein.ecommerce.api.domain.security.token.repository;

import com.melvstein.ecommerce.api.domain.security.token.document.UserToken;

import java.util.Optional;

public interface UserTokenRepositoryCustom {
    String findAvailableToken(String userId);
    Optional<UserToken> findAvailableTokenDetailsByUserId(String userId);
}
