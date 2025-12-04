package com.memorygame.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.memorygame.dto.UserResponseDto;
import com.memorygame.exception.UserNotFoundException;
import com.memorygame.model.ProviderType;
import com.memorygame.model.User;
import com.memorygame.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves User entity from database using OAuth2User information.
     */
    public User getUserFromOAuth2User(OAuth2User oauth2User) {
        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext()
                .getAuthentication();
        String registrationId = authentication.getAuthorizedClientRegistrationId();
        ProviderType providerType = ProviderType.valueOf(registrationId.toUpperCase());
        String providerId = oauth2User.getName();

        return userRepository.findByProviderAndProviderId(providerType, providerId)
                .orElseThrow(() -> new UserNotFoundException("인증된 사용자를 DB에서 찾을 수 없습니다."));
    }

    /**
     * Converts OAuth2User to UserResponseDto.
     */
    public UserResponseDto convertToDto(OAuth2User oauth2User) {
        if (oauth2User == null) {
            return UserResponseDto.builder()
                    .authenticated(false)
                    .build();
        }

        try {
            User user = getUserFromOAuth2User(oauth2User);

            return UserResponseDto.from(user);

        } catch (Exception e) {
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");

            if (name == null && email == null) {
                name = "인증된 사용자";
            }

            return UserResponseDto.builder()
                    .authenticated(true)
                    .name(name != null ? name : email)
                    .email(email)
                    .provider("UNKNOWN")
                    .build();
        }
    }
}
