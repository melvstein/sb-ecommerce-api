package com.melvstein.ecommerce.api.domain.security.service;

import com.melvstein.ecommerce.api.domain.security.document.UserToken;
import com.melvstein.ecommerce.api.domain.security.repository.UserTokenRepository;
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
}
