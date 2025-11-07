package com.memorygame.config;

import java.util.Collections;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.memorygame.model.User;
import com.memorygame.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 보안 필터 체인 설정
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 서버이므로)
            .authorizeHttpRequests(auth -> auth
                // 리더보드는 로그인 없이 접근 가능
                .requestMatchers(HttpMethod.GET, "/api/leaderboard").permitAll()
                // 정적 파일과 루트 페이지 접근 허용
                .requestMatchers("/", "/style/**", "/js/**", "/manifest.json", "/icons/**", "/service-worker.js").permitAll()
                // 점수 제출은 인증된 사용자만 가능
                .requestMatchers(HttpMethod.POST, "/api/score").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService())
                )
                .defaultSuccessUrl("/", true) // 로그인 성공 시 루트 페이지로 리다이렉트
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/") // 로그아웃 성공 시 루트 페이지로 리다이렉트
                .permitAll()
            );
        return http.build();
    }

    // CORS 설정
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드가 실행되는 주소를 명시적으로 허용해야 합니다.
        // 개발 환경에서는 와일드카드를 사용하지 않고 http://localhost:8080 등으로 설정하는 것이 좋습니다.
        // (예시를 위해 * 사용. 실제 서비스에서는 프론트엔드 도메인 지정)
        configuration.setAllowedOrigins(Collections.singletonList("*")); 
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        // 프론트엔드에서 `credentials: 'include'`를 사용하므로 필수
        configuration.setAllowCredentials(true); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // OAuth2 사용자 서비스: Google 로그인 후 사용자 정보를 DB에 저장하거나 업데이트
    @Bean
    OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return (userRequest) -> {
            OAuth2User oAuth2User = delegate.loadUser(userRequest);

            String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName(); // "sub" (Google)

            Map<String, Object> attributes = oAuth2User.getAttributes();
            String id = (String) attributes.get(userNameAttributeName); // Google Sub (고유 ID)
            String name = (String) attributes.get("name");
            String email = (String) attributes.get("email");

            // 사용자 정보 DB에 저장/업데이트
            User user = userRepository.findByProviderId(id)
                .map(entity -> entity.update(name, email))
                .orElse(User.builder()
                    .providerId(id)
                    .name(name)
                    .email(email)
                    .build());

            userRepository.save(user);

            // Spring Security가 사용할 OAuth2User 반환
            return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName
            );
        };
    }
}