package com.memorygame.dto;

import com.memorygame.model.ProviderType;
import com.memorygame.model.User;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * OAuth2 서비스별 응답 데이터를 파싱하여 통합된 형태로 저장하는 클래스
 */
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

    // ProviderType에 따라 적절한 of() 메서드를 호출하여 속성 파싱
    public static OAuthAttributes of(ProviderType providerType,
            String userNameAttributeName,
            Map<String, Object> attributes) {

        switch (providerType) {
            case NAVER:
                return ofNaver(userNameAttributeName, attributes);
            case KAKAO:
                return ofKakao(userNameAttributeName, attributes);
            case GOOGLE:
            default:
                // Apple 로그인은 별도 JWT 처리가 필요하나, Google과 유사한 필드 추출 방식을 사용한다고 가정
                return ofGoogle(userNameAttributeName, attributes);
        }
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .providerId((String) attributes.get(userNameAttributeName))
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .providerType(ProviderType.GOOGLE)
                .attributes(attributes)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        // 네이버는 "response" 키 아래에 실제 사용자 정보가 있음
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .nameAttributeKey("id") // 네이버는 id가 고유 식별자
                .providerId((String) response.get("id"))
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .providerType(ProviderType.NAVER)
                .attributes(response) // response를 attributes로 사용 (flattening)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        // 카카오는 "kakao_account" 아래에 이메일, "properties" 아래에 닉네임이 있음
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .providerId(String.valueOf(attributes.get(userNameAttributeName))) // 카카오 ID (Number 타입일 수 있어 String으로
                                                                                   // 변환)
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .providerType(ProviderType.KAKAO)
                .attributes(attributes)
                .build();
    }

    // User 엔티티 생성을 위한 메서드
    public User toEntity() {
        return User.builder()
                .provider(providerType)
                .providerId(providerId)
                .name(name)
                .email(email)
                .build();
    }
}
