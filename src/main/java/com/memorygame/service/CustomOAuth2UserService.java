package com.memorygame.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.memorygame.model.ProviderType;
import com.memorygame.model.User;
import com.memorygame.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 OAuth2User 로드
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 2. Provider 정보 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        ProviderType providerType = ProviderType.valueOf(registrationId.toUpperCase());
        
        // 3. 사용자 이름 속성 키 추출
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 4. Provider별 속성 파싱 및 통합
        OAuthAttributes attributes = OAuthAttributes.of(
                providerType, userNameAttributeName, oAuth2User.getAttributes());

        // 5. DB 저장 또는 업데이트 (Provider + ProviderId 조합으로 조회)
        saveOrUpdate(attributes);

        // 6. DefaultOAuth2User 반환 (인증 완료)
        // 실제 애플리케이션에서 사용할 Principal 객체로 반환할 수도 있지만, 여기서는 간단히 DefaultOAuth2User 사용
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }
    
    /**
     * ProviderType과 ProviderId를 함께 사용하여 DB에서 사용자를 찾고, 없으면 새로 생성 후 저장합니다.
     */
    private User saveOrUpdate(OAuthAttributes attributes) {
        // provider와 providerId를 모두 사용하여 유저를 찾습니다.
        User user = userRepository.findByProviderAndProviderId(attributes.getProviderType(), attributes.getProviderId())
                .map(entity -> entity.update(attributes.getName(), attributes.getEmail()))
                .orElse(attributes.toEntity()); // 새 유저인 경우 toEntity()로 User 생성

        return userRepository.save(user);
    }
}