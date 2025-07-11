package com.melvstein.ecommerce.api.domain.security.authentication.usertoken.repository;

import com.melvstein.ecommerce.api.domain.security.authentication.usertoken.document.UserToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends MongoRepository<UserToken, String>, UserTokenRepositoryCustom {
    Optional<UserToken> findByToken(String token);
    Optional<UserToken> findByUserId(String userId);
}
