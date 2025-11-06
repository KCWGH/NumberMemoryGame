package com.memorygame;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/css/**", "/style/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/api/ping", "/api/leaderboard", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(new RequestMatcher() {
                    @Override
                    public boolean matches(jakarta.servlet.http.HttpServletRequest request) {
                        String path = request.getRequestURI();
                        return path != null && path.startsWith("/h2-console/");
                    }
                })
            )
            .headers(headers -> 
                headers.addHeaderWriter(
                    new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)
                )
            )
            .cors(cors -> {});

        return http.build();
    }
}
