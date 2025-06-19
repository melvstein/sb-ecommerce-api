package com.melvstein.ecommerce.api.domain.security.authentication.usertoken.service;

import com.melvstein.ecommerce.api.domain.security.authentication.usertoken.document.UserToken;
import com.melvstein.ecommerce.api.domain.security.authentication.usertoken.repository.UserTokenRepository;
import com.melvstein.ecommerce.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserTokenService {
    private final UserTokenRepository userTokenRepository;
    private final JwtService jwtService;

    public UserToken saveUserToken(UserToken userToken) {
        return userTokenRepository.save(userToken);
    }

    public Optional<UserToken> fetchTokenDetails(String token) {
        return userTokenRepository.findByToken(token);
    }

    public String getAvailableToken(String userId) {
        return userTokenRepository.findAvailableToken(userId);
    }

    public Optional<UserToken> getAvailableTokenDetailsByUserId(String userId) {
        return userTokenRepository.findAvailableTokenDetailsByUserId(userId);
    }

    public Optional<UserToken> fetchDetailsByUserId(String userId) {
        return userTokenRepository.findByUserId(userId);
    }

    public UserToken generatedUserToken(String userId, String accessToken) {
        return UserToken.builder()
                .token(accessToken)
                .userId(userId)
                .type("jwt")
                .timeout(jwtService.getExpirationTimeSeconds())
                .expiredAt(jwtService.extractExpiration(accessToken).toInstant())
                .build();
    }
}
