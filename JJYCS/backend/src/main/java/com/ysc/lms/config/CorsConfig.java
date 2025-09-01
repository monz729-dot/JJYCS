package com.ysc.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private List<String> allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private List<String> allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private Long maxAge;

    @Bean
    @Profile("dev")
    public CorsConfigurationSource corsConfigurationSourceDev() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 개발 환경: localhost 포트 범위 허용
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*"
        ));
        
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    @Bean
    @Profile("!dev")
    public CorsConfigurationSource corsConfigurationSourceProd() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 운영 환경: application.yml에서 설정된 도메인만 허용
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            configuration.setAllowedOrigins(allowedOrigins);
        } else {
            // 기본값: 보안을 위해 아무것도 허용하지 않음
            throw new IllegalStateException(
                "CORS allowed origins must be configured for production environment. " +
                "Please set 'cors.allowed-origins' in application.yml or environment variables."
            );
        }
        
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With"
        ));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);
        
        // 보안 헤더 추가 설정
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Disposition"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}