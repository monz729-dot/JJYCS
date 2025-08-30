package com.ysc.lms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * RestTemplate 설정 및 재시도 메커니즘
 */
@Configuration
@EnableRetry
@Slf4j
public class RestTemplateConfig {
    
    @Value("${carrier.api.connection-timeout:5000}")
    private int connectionTimeout;
    
    @Value("${carrier.api.read-timeout:10000}")
    private int readTimeout;
    
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        
        // 타임아웃 설정
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // 로깅 인터셉터 추가
        restTemplate.setInterceptors(Collections.singletonList(loggingInterceptor()));
        
        return restTemplate;
    }
    
    /**
     * HTTP 요청/응답 로깅 인터셉터
     */
    @Bean
    public ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            
            log.debug("[{}] HTTP {} {} - Request Body: {}", 
                timestamp, request.getMethod(), request.getURI(), new String(body));
            
            var response = execution.execute(request, body);
            
            log.debug("[{}] HTTP Response Status: {} - Headers: {}", 
                timestamp, response.getStatusCode(), response.getHeaders());
            
            return response;
        };
    }
}