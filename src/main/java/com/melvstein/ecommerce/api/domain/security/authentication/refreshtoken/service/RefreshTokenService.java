package com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.service;

import com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.document.RefreshToken;
import com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.repository.RefreshTokenRepository;
import com.melvstein.ecommerce.api.domain.user.document.User;
import com.melvstein.ecommerce.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.security.refresh.token.timeout}")
    private long timeout;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final MongoTemplate mongoTemplate;

    public Optional<RefreshToken> fetchRefreshTokenDetails(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isTokenValid(String token) {
        return refreshTokenRepository.isValid(token);
    }

    public void deleteToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public boolean deleteAllTokensByUserId(String userId) {
        long deletedCount = refreshTokenRepository.deleteAllByUserId(userId);
        return deletedCount > 0;
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public RefreshToken generatedRefreshToken(User user) {
        String token = jwtService.generateRefreshToken(user.getUsername());
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(user.getId())
                .timeout(timeout)
                .expiredAt(Instant.now().plusSeconds(timeout))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken saveRefreshToken(RefreshToken refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken.getToken()).orElseGet(() -> refreshTokenRepository.save(refreshToken));
    }

    public Optional<RefreshToken> getAvailableToken(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria
                .where("userId").is(userId)
                .and("expiredAt").gt(Instant.now()));

        return  Optional.ofNullable(mongoTemplate.findOne(query, RefreshToken.class));
    }
}
