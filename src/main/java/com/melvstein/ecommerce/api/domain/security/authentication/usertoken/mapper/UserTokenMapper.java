package com.melvstein.ecommerce.api.domain.security.authentication.usertoken.mapper;


import com.melvstein.ecommerce.api.domain.security.authentication.usertoken.document.UserToken;
import com.melvstein.ecommerce.api.domain.security.authentication.usertoken.dto.UserTokenDto;
import com.melvstein.ecommerce.api.shared.util.Utils;

public class UserTokenMapper {

    public static UserTokenDto toDto(UserToken userToken) {
        return UserTokenDto.builder()
                .id(userToken.getId())
                .token(userToken.getToken())
                .userId(userToken.getUserId())
                .type(userToken.getType())
                .timeout(userToken.getTimeout())
                .isAvailable(userToken.isAvailable())
                .expiredAt(Utils.fromInstantToDate(userToken.getExpiredAt()))
                .createdAt(Utils.fromInstantToDate(userToken.getCreatedAt()))
                .updatedAt(Utils.fromInstantToDate(userToken.getUpdatedAt()))
                .build();
    }

    public static UserToken toDocument(UserTokenDto userTokenDto) {
        return UserToken.builder()
                .id(userTokenDto.id())
                .token(userTokenDto.token())
                .userId(userTokenDto.userId())
                .type(userTokenDto.type())
                .timeout(userTokenDto.timeout())
                .isAvailable(userTokenDto.isAvailable())
                .expiredAt(userTokenDto.expiredAt().toInstant())
                .createdAt(userTokenDto.createdAt().toInstant())
                .updatedAt(userTokenDto.updatedAt().toInstant())
                .build();
    }
}
