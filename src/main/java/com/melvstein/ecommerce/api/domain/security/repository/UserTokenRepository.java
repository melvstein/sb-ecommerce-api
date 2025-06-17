package com.melvstein.ecommerce.api.domain.security.repository;

import com.melvstein.ecommerce.api.domain.security.document.UserToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends MongoRepository<UserToken, String> {
    public Optional<UserToken> findByToken(String token);
}
