package com.melvstein.ecommerce.api.domain.security.authentication.usertoken.repository;

import com.melvstein.ecommerce.api.domain.security.authentication.usertoken.document.UserToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class UserTokenRepositoryImpl implements UserTokenRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public String findAvailableToken(String userId) {
        Query query = new Query();
        query.fields().include("token");
        query.addCriteria(Criteria
                .where("userId").is(userId)
                .and("expiredAt").gt(Instant.now()));

        UserToken userToken = mongoTemplate.findOne(query, UserToken.class);

        return userToken != null ? userToken.getToken() : null;
    }

    @Override
    public Optional<UserToken> findAvailableTokenDetailsByUserId(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria
                .where("userId").is(userId)
                .and("expiredAt").gt(Instant.now()));

        UserToken userToken = mongoTemplate.findOne(query, UserToken.class);

        return Optional.ofNullable(userToken);
    }
}