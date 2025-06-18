package com.melvstein.ecommerce.api.domain.security.token.service;

import com.melvstein.ecommerce.api.domain.security.token.document.UserToken;
import com.melvstein.ecommerce.api.domain.security.token.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserTokenService {
    private final UserTokenRepository userTokenRepository;

    public UserToken saveToken(UserToken userToken) {
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
}
