package com.ycs.lms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
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
    @Order(0)
    public SecurityFilterChain h2ConsoleChain(HttpSecurity http) throws Exception {
        http
            // 컨텍스트패스(/api)와 무관하게 servletPath는 /h2-console 로 들어옵니다.
            .securityMatcher(PathRequest.toH2Console())
            .csrf(csrf -> csrf.disable())
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .authorizeHttpRequests(a -> a.anyRequest().permitAll());
        
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
                // 공개 경로 (/api 프리픽스 포함)
                .requestMatchers(
                    "/api/auth/**",
                    "/auth/**",
                    "/api/public/**",
                    "/public/**",
                    "/v3/api-docs/**", 
                    "/swagger-ui/**", 
                    "/swagger-ui.html",
                    "/actuator/health",
                    "/api/actuator/health",
                    "/favicon.ico",
                    "/api/labels/**",
                    "/labels/**"
                ).permitAll()
                
                // 관리자 전용
                .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                
                // 창고 및 관리자
                .requestMatchers("/api/warehouse/**", "/warehouse/**").hasAnyRole("WAREHOUSE", "ADMIN")
                
                // 파트너 전용
                .requestMatchers("/api/partner/**", "/partner/**").hasRole("PARTNER")
                
                // 주문 관련 - 일반/기업/관리자/창고
                .requestMatchers("/api/orders/**", "/orders/**").hasAnyRole("GENERAL", "CORPORATE", "ADMIN", "WAREHOUSE")
                
                // 라벨 생성 - 일반/기업/관리자/창고
                .requestMatchers("/api/labels/**", "/labels/**").hasAnyRole("GENERAL", "CORPORATE", "ADMIN", "WAREHOUSE")
                
                // 비즈니스 룰 테스트용 - 임시 허용
                .requestMatchers("/api/orders/calculate-cbm", "/api/orders/validate", "/api/orders/validate-business-rules").permitAll()
                .requestMatchers("/orders/calculate-cbm", "/orders/validate", "/orders/validate-business-rules").permitAll()
                
                // HS Code 조회 - 임시로 모든 요청 허용 (테스트용)
                .requestMatchers("/api/hscode/**", "/hscode/**").permitAll()
                
                // 은행계좌 - 일반/기업/관리자
                .requestMatchers("/api/bank-accounts/**", "/bank-accounts/**").hasAnyRole("GENERAL", "CORPORATE", "ADMIN")
                
                // 기타 모든 요청은 인증 필요
                .anyRequest().authenticated()
            );

        return http.build();
    }

}