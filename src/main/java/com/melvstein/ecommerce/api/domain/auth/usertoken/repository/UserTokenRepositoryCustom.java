package com.melvstein.ecommerce.api.domain.auth.usertoken.repository;

import com.melvstein.ecommerce.api.domain.auth.usertoken.document.UserToken;

import java.util.Optional;

public interface UserTokenRepositoryCustom {
    String findAvailableToken(String userId);
    Optional<UserToken> findAvailableTokenDetailsByUserId(String userId);
}
