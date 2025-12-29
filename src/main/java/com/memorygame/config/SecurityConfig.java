package com.memorygame.config;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.memorygame.service.CustomOAuth2UserService;
import com.memorygame.service.RateLimiterService;
import com.memorygame.exception.RateLimitExceededException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Value("${cors.allowed-origin}")
        private String allowedOrigin;

        private final CustomOAuth2UserService customOAuth2UserService;
        private final RateLimiterService rateLimiterService;

        public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, RateLimiterService rateLimiterService) {
                this.customOAuth2UserService = customOAuth2UserService;
                this.rateLimiterService = rateLimiterService;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
                CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
                requestHandler.setCsrfRequestAttributeName(null);

                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(tokenRepository)
                                                .csrfTokenRequestHandler(requestHandler))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.GET, "/api/leaderboard").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/user").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/game/start").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/game/end").authenticated()
                                                .requestMatchers("/index.html", "/style/**", "/js/**", "/manifest.json",
                                                                "/icons/**",
                                                                "/service-worker.js")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .defaultSuccessUrl("/", true))
                                .logout(logout -> logout
                                                .logoutUrl("/api/logout")
                                                .logoutSuccessUrl("/")
                                                .permitAll())
                                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                                .addFilterBefore(new RateLimitFilter(rateLimiterService),
                                                BasicAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Collections.singletonList(allowedOrigin));
                configuration.setAllowedMethods(Collections.singletonList("*"));
                configuration.setAllowedHeaders(Collections.singletonList("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        private static class CsrfCookieFilter extends OncePerRequestFilter {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                FilterChain filterChain)
                                throws ServletException, IOException {
                        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                        if (csrfToken != null) {
                                csrfToken.getToken();
                        }
                        filterChain.doFilter(request, response);
                }
        }

        private static class RateLimitFilter extends OncePerRequestFilter {
                private final RateLimiterService rateLimiterService;

                public RateLimitFilter(RateLimiterService rateLimiterService) {
                        this.rateLimiterService = rateLimiterService;
                }

                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                FilterChain filterChain)
                                throws ServletException, IOException {
                        String requestUri = request.getRequestURI();

                        if (requestUri.startsWith("/api/leaderboard") || requestUri.startsWith("/api/game")) {
                                String identifier = getIdentifier(request);

                                if (!rateLimiterService.allowRequest(identifier)) {
                                        throw new RateLimitExceededException("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
                                }
                        }

                        filterChain.doFilter(request, response);
                }

                private String getIdentifier(HttpServletRequest request) {
                        if (request.getUserPrincipal() != null) {
                                return "user:" + request.getUserPrincipal().getName();
                        }

                        String ip = request.getHeader("X-Forwarded-For");
                        if (ip == null || ip.isEmpty()) {
                                ip = request.getRemoteAddr();
                        }
                        return "ip:" + ip;
                }
        }
}