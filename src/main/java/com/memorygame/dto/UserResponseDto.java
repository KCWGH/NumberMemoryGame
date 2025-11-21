package com.memorygame.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private boolean authenticated;
    private String name;
    private String email;
    private String provider;
}
