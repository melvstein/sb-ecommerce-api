package com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.mapper;

import com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.document.RefreshToken;
import com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.dto.RefreshTokenDto;
import com.melvstein.ecommerce.api.shared.util.Utils;

public class RefreshTokenMapper {

    public RefreshTokenDto toDto(RefreshToken refreshToken) {
        if (refreshToken == null) return null;

        return RefreshTokenDto.builder()
                .id(refreshToken.getId())
                .token(refreshToken.getToken())
                .userId(refreshToken.getUserId())
                .timeout(refreshToken.getTimeout())
                .expiredAt(Utils.fromInstantToDate(refreshToken.getExpiredAt()))
                .createdAt(Utils.fromInstantToDate(refreshToken.getCreatedAt()))
                .updatedAt(Utils.fromInstantToDate(refreshToken.getUpdatedAt()))
                .build();
    }

    public RefreshToken toDocument(RefreshTokenDto refreshTokenDto) {
        if (refreshTokenDto == null) return null;

        return RefreshToken.builder()
                .id(refreshTokenDto.id())
                .token(refreshTokenDto.token())
                .userId(refreshTokenDto.userId())
                .timeout(refreshTokenDto.timeout())
                .expiredAt(refreshTokenDto.expiredAt().toInstant())
                .createdAt(refreshTokenDto.createdAt().toInstant())
                .updatedAt(refreshTokenDto.updatedAt().toInstant())
                .build();
    }
}