package com.ysc.lms.config;

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
                    "/favicon.ico"
                ).permitAll()
                
                // 입고확인 API - 관리자 또는 창고직원
                .requestMatchers("/api/admin/inbound/**").hasAnyRole("ADMIN", "WAREHOUSE")
                
                // 관리자 전용 - 가장 구체적인 경로부터 매칭
                .requestMatchers("/api/admin/users/pending").hasRole("ADMIN")
                .requestMatchers("/api/admin/users/{id}/approve").hasRole("ADMIN")
                .requestMatchers("/api/admin/users/{id}/reject").hasRole("ADMIN")
                .requestMatchers("/api/admin/performance/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/users").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 창고 및 관리자
                .requestMatchers("/api/warehouse/**", "/warehouse/**").hasAnyRole("WAREHOUSE", "ADMIN")
                
                // 파트너 전용
                .requestMatchers("/api/partner/**", "/partner/**").hasRole("PARTNER")
                
                // 주문 생성 - 승인 대기 중인 사용자도 주문 생성 가능 (제한된 기능)
                .requestMatchers("/api/orders", "/orders").hasAnyRole("GENERAL", "CORPORATE", "ADMIN", "WAREHOUSE", "PENDING")
                .requestMatchers("/api/orders/validate*", "/orders/validate*").hasAnyRole("GENERAL", "CORPORATE", "ADMIN", "WAREHOUSE", "PENDING")
                .requestMatchers("/api/orders/calculate-cbm", "/orders/calculate-cbm").hasAnyRole("GENERAL", "CORPORATE", "ADMIN", "WAREHOUSE", "PENDING")
                
                // 주문 조회 및 통계 - 모든 인증된 사용자 허용
                .requestMatchers("/api/orders/user/me/**", "/orders/user/me/**").authenticated()
                .requestMatchers("/api/orders/user/*/stats", "/orders/user/*/stats").authenticated()
                
                // 주문 관리 - 일반/기업/관리자/창고만 (PENDING 제외)
                .requestMatchers("/api/orders/**", "/orders/**").hasAnyRole("GENERAL", "CORPORATE", "ADMIN", "WAREHOUSE")
                
                // 라벨 생성 - 일반/기업/관리자/창고
                .requestMatchers("/api/labels/**", "/labels/**").hasAnyRole("GENERAL", "CORPORATE", "ADMIN", "WAREHOUSE")
                
                // HS Code 조회 - 인증된 사용자만 허용 (GENERAL 사용자 포함)
                .requestMatchers("/api/hscode/**", "/hscode/**").hasAnyRole("ADMIN", "WAREHOUSE", "PARTNER", "CORPORATE", "GENERAL")
                
                // 은행계좌 - 일반/기업/관리자
                .requestMatchers("/api/bank-accounts/**", "/bank-accounts/**").hasAnyRole("GENERAL", "CORPORATE", "ADMIN")
                
                // 대시보드 API - 모든 인증된 사용자 허용
                .requestMatchers("/api/dashboard/**", "/dashboard/**").authenticated()
                
                // 기타 모든 요청은 인증 필요
                .anyRequest().authenticated()
            );

        return http.build();
    }

}