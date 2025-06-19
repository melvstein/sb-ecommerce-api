package com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.repository;

import com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.document.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;


@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public boolean isValid(String token) {
        Query query = new Query();
        query.addCriteria(Criteria.where("token").is(token)
                .and("expiredAt").gt(Instant.now()));

        return mongoTemplate.exists(query, RefreshToken.class);
    }
}
