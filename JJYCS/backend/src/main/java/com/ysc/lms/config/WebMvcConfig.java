package com.ysc.lms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final PerformanceMonitoringInterceptor performanceMonitoringInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 성능 모니터링 인터셉터 등록
        registry.addInterceptor(performanceMonitoringInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/actuator/**",  // Actuator 엔드포인트 제외
                    "/api/h2-console/**", // H2 콘솔 제외
                    "/api/swagger-ui/**", // Swagger UI 제외
                    "/api/v3/api-docs/**" // API Docs 제외
                );
    }
}