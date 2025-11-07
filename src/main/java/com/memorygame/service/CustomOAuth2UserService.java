package com.memorygame.service;

import com.memorygame.model.User;
import com.memorygame.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 서비스 로직 호출 (Google에서 사용자 정보 가져옴)
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // 2. 사용자 정보 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // Google ID (sub)를 식별자로 사용
        String oauthId = (String) attributes.get("sub"); 
        String name = (String) attributes.get("name");
        String email = (String) attributes.get("email");

        // 3. User 엔티티 저장 또는 업데이트
        User user = userRepository.findById(oauthId)
                .orElse(null); // DB에 사용자가 없는 경우 null

        if (user == null) {
            // 새 사용자 등록
            user = new User(oauthId, email, name);
        } else {
            // 기존 사용자 정보 업데이트 (이름 변경 등에 대비)
            user.setName(name);
            user.setEmail(email);
        }
        
        userRepository.save(user);

        // 4. 세션에 저장될 OAuth2User 반환 (DB ID를 Principal로 사용)
        return oAuth2User; 
    }
}