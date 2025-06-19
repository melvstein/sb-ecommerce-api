package com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.service;

import com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.document.RefreshToken;
import com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.security.refresh.token.timeout}")
    private long timeout;

    private final RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshToken> fetchRefreshTokenDetails(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isTokenValid(String token) {
        return refreshTokenRepository.isValid(token);
    }

    public void deleteToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public RefreshToken generatedRefreshToken(String userId) {
        String token = generateToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .timeout(timeout)
                .expiredAt(Instant.now().plusSeconds(timeout))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken saveRefreshToken(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }
}
