package com.ysc.lms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
                    "/api/swagger-ui/**", // Swagger UI 제외
                    "/api/v3/api-docs/**" // API Docs 제외
                );
    }
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // UTF-8 인코딩을 위한 StringHttpMessageConverter 추가
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converters.add(0, stringConverter);
        
        // JSON 메시지 컨버터도 UTF-8 설정
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(1, jsonConverter);
    }
}