package com.ysc.lms.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * API 응답 시간 모니터링 인터셉터
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceMonitoringInterceptor implements HandlerInterceptor {
    
    private static final String START_TIME_ATTRIBUTE = "startTime";
    
    // 성능 통계 저장
    private final ConcurrentHashMap<String, EndpointStats> endpointStats = new ConcurrentHashMap<>();
    
    @Value("${app.monitoring.performance.enabled:true}")
    private boolean monitoringEnabled;
    
    @Value("${app.monitoring.performance.slow-request-threshold:1000}")
    private long slowRequestThreshold;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (monitoringEnabled) {
            request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        if (!monitoringEnabled) {
            return;
        }
        
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime == null) {
            return;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        String endpoint = getEndpointKey(request);
        int statusCode = response.getStatus();
        
        // 통계 업데이트
        updateStats(endpoint, duration, statusCode);
        
        // 느린 요청 로깅
        if (duration >= slowRequestThreshold) {
            log.warn("Slow request detected - Endpoint: {}, Method: {}, Duration: {}ms, Status: {}, User: {}", 
                    endpoint, 
                    request.getMethod(), 
                    duration, 
                    statusCode,
                    getCurrentUser(request));
        }
        
        // 일반 요청 로깅 (DEBUG 레벨)
        log.debug("API Request completed - Endpoint: {}, Method: {}, Duration: {}ms, Status: {}", 
                endpoint, request.getMethod(), duration, statusCode);
    }
    
    private void updateStats(String endpoint, long duration, int statusCode) {
        endpointStats.computeIfAbsent(endpoint, k -> new EndpointStats())
                    .updateStats(duration, statusCode);
    }
    
    private String getEndpointKey(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        
        // 경로 파라미터를 일반화 (예: /api/users/123 -> /api/users/{id})
        uri = normalizeUri(uri);
        
        return method + " " + uri;
    }
    
    private String normalizeUri(String uri) {
        // ID 패턴을 {id}로 치환
        uri = uri.replaceAll("/\\d+", "/{id}");
        
        // UUID 패턴을 {uuid}로 치환
        uri = uri.replaceAll("/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "/{uuid}");
        
        return uri;
    }
    
    private String getCurrentUser(HttpServletRequest request) {
        // JWT 토큰에서 사용자 정보 추출
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 간단히 토큰의 첫 부분만 로깅 (보안상 전체 토큰은 로깅하지 않음)
            return authHeader.substring(7, Math.min(27, authHeader.length())) + "...";
        }
        return "anonymous";
    }
    
    /**
     * 엔드포인트 통계 정보
     */
    public static class EndpointStats {
        private final AtomicLong totalRequests = new AtomicLong();
        private final AtomicLong totalDuration = new AtomicLong();
        private final AtomicLong minDuration = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxDuration = new AtomicLong();
        private final AtomicLong errorCount = new AtomicLong();
        private volatile long lastUpdateTime = System.currentTimeMillis();
        
        public void updateStats(long duration, int statusCode) {
            totalRequests.incrementAndGet();
            totalDuration.addAndGet(duration);
            
            // Min/Max 업데이트
            minDuration.updateAndGet(current -> Math.min(current, duration));
            maxDuration.updateAndGet(current -> Math.max(current, duration));
            
            // 에러 카운트 (4xx, 5xx)
            if (statusCode >= 400) {
                errorCount.incrementAndGet();
            }
            
            lastUpdateTime = System.currentTimeMillis();
        }
        
        public double getAverageResponseTime() {
            long total = totalRequests.get();
            return total > 0 ? (double) totalDuration.get() / total : 0.0;
        }
        
        public double getErrorRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) errorCount.get() / total * 100 : 0.0;
        }
        
        // Getters
        public long getTotalRequests() { return totalRequests.get(); }
        public long getTotalDuration() { return totalDuration.get(); }
        public long getMinDuration() { 
            long min = minDuration.get();
            return min == Long.MAX_VALUE ? 0 : min; 
        }
        public long getMaxDuration() { return maxDuration.get(); }
        public long getErrorCount() { return errorCount.get(); }
        public long getLastUpdateTime() { return lastUpdateTime; }
    }
    
    /**
     * 모든 엔드포인트 통계 조회
     */
    public ConcurrentHashMap<String, EndpointStats> getAllStats() {
        return new ConcurrentHashMap<>(endpointStats);
    }
    
    /**
     * 특정 엔드포인트 통계 조회
     */
    public EndpointStats getEndpointStats(String endpoint) {
        return endpointStats.get(endpoint);
    }
    
    /**
     * 통계 초기화
     */
    public void clearStats() {
        endpointStats.clear();
        log.info("Performance monitoring statistics cleared");
    }
}