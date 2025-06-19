package com.melvstein.ecommerce.api.domain.auth.refreshtoken.repository;

import com.melvstein.ecommerce.api.domain.auth.refreshtoken.document.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String>, RefreshTokenRepositoryCustom {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
}
