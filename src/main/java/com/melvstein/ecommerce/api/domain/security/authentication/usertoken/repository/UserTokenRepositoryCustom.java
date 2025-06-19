package com.melvstein.ecommerce.api.domain.security.authentication.usertoken.repository;

import com.melvstein.ecommerce.api.domain.security.authentication.usertoken.document.UserToken;

import java.util.Optional;

public interface UserTokenRepositoryCustom {
    String findAvailableToken(String userId);
    Optional<UserToken> findAvailableTokenDetailsByUserId(String userId);
}
