package com.memorygame.dto;

import java.util.Map;

import com.memorygame.model.ProviderType;
import com.memorygame.model.User;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthAttributes {
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String providerId;
    private final String name;
    private final String email;
    private final ProviderType providerType;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
            String providerId, String name, String email, ProviderType providerType) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.providerId = providerId;
        this.name = name;
        this.email = email;
        this.providerType = providerType;
    }

    public static OAuthAttributes of(ProviderType providerType,
            String userNameAttributeName,
            Map<String, Object> attributes) {

        switch (providerType) {
            case NAVER:
                return ofNaver(attributes);
            case KAKAO:
                return ofKakao(userNameAttributeName, attributes);
            case GOOGLE:
            default:
                return ofGoogle(userNameAttributeName, attributes);
        }
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String nickname = email.split("@")[0];
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .providerId((String) attributes.get(userNameAttributeName))
                .name(nickname)
                .email(email)
                .providerType(ProviderType.GOOGLE)
                .attributes(attributes)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        String email = (String) response.get("email");
        String providerId = (String) response.get("id");
        String nickname = (String) response.get("nickname");

        String displayName;

        if (email != null && email.contains("@")) {
            displayName = email.split("@")[0];
        } else if (nickname != null && !nickname.isBlank()) {
            displayName = nickname;
        } else {
            displayName = (providerId != null && providerId.length() > 8)
                    ? providerId.substring(0, 8)
                    : providerId;
        }

        return OAuthAttributes.builder()
                .nameAttributeKey("id")
                .providerId(providerId)
                .name(displayName)
                .email(email)
                .providerType(ProviderType.NAVER)
                .attributes(response)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        String nickname = email.split("@")[0];

        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .providerId(String.valueOf(attributes.get(userNameAttributeName)))
                .name(nickname)
                .email(email)
                .providerType(ProviderType.KAKAO)
                .attributes(attributes)
                .build();
    }

    public User toEntity() {
        return User.builder()
                .provider(providerType)
                .providerId(providerId)
                .name(name)
                .email(email)
                .build();
    }
}
