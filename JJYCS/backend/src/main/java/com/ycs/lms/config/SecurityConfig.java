package com.ycs.lms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 1) H2 콘솔 전용 체인 (항상 먼저 평가되도록 Order 낮게)
    @Bean
    @Order(1)
    public SecurityFilterChain h2ConsoleChain(HttpSecurity http) throws Exception {
        http
            // 컨텍스트 패스가 /api 여도 ServletPath는 /h2-console 로 잡힘
            .securityMatcher(new AntPathRequestMatcher("/h2-console/**"))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
        
        return http.build();
    }

    // 2) 나머지 API 체인
    @Bean
    @Order(2)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // CSRF 비활성화 (REST API용)
            .csrf(csrf -> csrf.disable())
            
            // 세션 관리 설정 - JWT 사용으로 STATELESS
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 권한 설정
            .authorizeHttpRequests(authz -> authz
                // 공개 경로
                .requestMatchers(
                    "/auth/**",
                    "/public/**",
                    "/v3/api-docs/**", 
                    "/swagger-ui/**", 
                    "/swagger-ui.html",
                    "/actuator/health",
                    "/favicon.ico"
                ).permitAll()
                
                // 관리자 전용
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // 창고 및 관리자
                .requestMatchers("/warehouse/**").hasAnyRole("WAREHOUSE", "ADMIN")
                
                // 파트너 전용
                .requestMatchers("/partner/**").hasRole("PARTNER")
                
                // 주문 관련 - 일반/기업/관리자/창고
                .requestMatchers("/orders/**").hasAnyRole("GENERAL", "CORPORATE", "ADMIN", "WAREHOUSE")
                
                // 은행계좌 - 일반/기업/관리자
                .requestMatchers("/bank-accounts/**").hasAnyRole("GENERAL", "CORPORATE", "ADMIN")
                
                // 기타 모든 요청은 인증 필요
                .anyRequest().authenticated()
            );

        return http.build();
    }

}