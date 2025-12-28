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

import com.memorygame.dto.OAuthAttributes;
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
                OAuth2User oAuth2User = delegate.loadUser(userRequest);

                String registrationId = userRequest.getClientRegistration().getRegistrationId();
                ProviderType providerType = ProviderType.valueOf(registrationId.toUpperCase());

                String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                                .getUserInfoEndpoint().getUserNameAttributeName();

                OAuthAttributes attributes = OAuthAttributes.of(
                                providerType, userNameAttributeName, oAuth2User.getAttributes());

                saveOrUpdate(attributes);

                return new DefaultOAuth2User(
                                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                                attributes.getAttributes(),
                                attributes.getNameAttributeKey());
        }

        private User saveOrUpdate(OAuthAttributes attributes) {
                User user = userRepository
                                .findByProviderAndProviderId(attributes.getProviderType(), attributes.getProviderId())
                                .map(entity -> entity.update(attributes.getName(), attributes.getEmail()))
                                .orElse(attributes.toEntity());

                return userRepository.save(user);
        }
}