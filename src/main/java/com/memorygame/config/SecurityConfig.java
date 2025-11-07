package com.memorygame.config;

import com.memorygame.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
// import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // ⚠️ 이 import는 제거해야 합니다.

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 비활성화 및 H2 콘솔 프레임 허용
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            
            // 2. 요청별 인가 설정
            .authorizeHttpRequests(authorize -> authorize
                // 정적 리소스, 리더보드 조회, H2 콘솔, 로그인 시작점 허용
                .requestMatchers(
                    "/", "/index.html", "/style.css", "/script.js", "/api/leaderboard", "/api/user",
                    "/h2-console/**", "/login/**", "/error"
                ).permitAll()
                // 점수 제출 API는 인증된 사용자만 접근 허용
                .requestMatchers("/api/score").authenticated()
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            
            // 3. OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService) 
                )
                .defaultSuccessUrl("/", true) 
            )

            // 4. 로그아웃 설정 (Deprecation 해결)
            .logout(logout -> logout
                // ⭐️⭐️⭐️ Deprecated된 AntPathRequestMatcher 대신 최신 방식 사용
                // 기본값이 "/logout"이므로 사실상 .logoutRequestMatcher()를 호출하지 않아도 되지만, 
                // 명시적으로 POST 요청에 대한 /logout을 처리하려면 아래와 같이 간결하게 설정합니다.
                // .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) 대신
                
                // Spring Security 6+에서는 기본적으로 POST /logout을 사용합니다.
                // 특별한 경로 변경이 없다면 아래 코드는 생략 가능하거나
                // 기본 설정으로 유지됩니다. (경고 발생 코드 제거)

                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()) 
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}