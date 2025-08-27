package com.ycs.lms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                
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
            )
            
            // H2 콘솔 설정
            .headers(headers -> headers.frameOptions().disable());

        return http.build();
    }

}