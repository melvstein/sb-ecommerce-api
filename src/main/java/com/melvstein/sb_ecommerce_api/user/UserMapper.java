package com.melvstein.sb_ecommerce_api.user;

import com.melvstein.sb_ecommerce_api.util.Utils;

public class UserMapper {

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .role(user.getRole())
                .email(user.getEmail())
                .username(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .lastLoginAt(Utils.fromInstantToDate(user.getLastLoginAt()))
                .createdAt(Utils.fromInstantToDate(user.getCreatedAt()))
                .updatedAt(Utils.fromInstantToDate(user.getUpdatedAt()))
                .deletedAt(Utils.fromInstantToDate(user.getDeletedAt()))
                .build();
    }

    public static User toDocument(UserDto userDto) {
        return User.builder()
                .id(userDto.id())
                .role(userDto.role())
                .email(userDto.email())
                .username(userDto.username())
                .profileImageUrl(userDto.profileImageUrl())
                .isActive(userDto.isActive())
                .isVerified(userDto.isVerified())
                .lastLoginAt(userDto.lastLoginAt().toInstant())
                .createdAt(userDto.createdAt().toInstant())
                .updatedAt(userDto.updatedAt().toInstant())
                .deletedAt(userDto.deletedAt().toInstant())
                .build();
    }
}
