package com.memorygame.dto;

import lombok.Builder;
import lombok.Getter;

import com.memorygame.model.User;

@Getter
@Builder
public class UserResponseDto {
    private boolean authenticated;
    private String id;
    private String email;
    private String provider;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .authenticated(true)
                .id(user.getName())
                .email(user.getEmail())
                .provider(user.getProvider().toString())
                .build();
    }
}
