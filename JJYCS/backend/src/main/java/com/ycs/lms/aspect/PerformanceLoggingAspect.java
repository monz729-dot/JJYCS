package com.ycs.lms.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@Slf4j
public class PerformanceLoggingAspect {

    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");
    private static final long SLOW_QUERY_THRESHOLD = 1000; // 1초

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logControllerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        String methodName = joinPoint.getSignature().toShortString();
        
        MDC.put("traceId", traceId);
        
        long startTime = System.currentTimeMillis();
        performanceLogger.info("REQUEST_START: {} - {}", traceId, methodName);
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            performanceLogger.info("REQUEST_END: {} - {} - {}ms", 
                traceId, methodName, executionTime);
            
            if (executionTime > SLOW_QUERY_THRESHOLD) {
                performanceLogger.warn("SLOW_REQUEST: {} - {} - {}ms (임계값: {}ms 초과)", 
                    traceId, methodName, executionTime, SLOW_QUERY_THRESHOLD);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceLogger.error("REQUEST_ERROR: {} - {} - {}ms - {}", 
                traceId, methodName, executionTime, e.getMessage());
            throw e;
        } finally {
            MDC.remove("traceId");
        }
    }

    @Around("execution(* com.ycs.lms.service.*.*(..))")
    public Object logServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > 500) { // 서비스 메소드 500ms 임계값
                performanceLogger.info("SERVICE_SLOW: {} - {}ms", methodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceLogger.error("SERVICE_ERROR: {} - {}ms - {}", 
                methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* com.ycs.lms.repository.*.*(..))")
    public Object logRepositoryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > 200) { // 데이터베이스 쿼리 200ms 임계값
                performanceLogger.warn("DB_SLOW_QUERY: {} - {}ms", methodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceLogger.error("DB_ERROR: {} - {}ms - {}", 
                methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* com.ycs.lms.service.external.*.*(..))")
    public Object logExternalApiPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            performanceLogger.info("EXTERNAL_API: {} - {}ms", methodName, executionTime);
            
            if (executionTime > 5000) { // 외부 API 5초 임계값
                performanceLogger.warn("EXTERNAL_API_SLOW: {} - {}ms", methodName, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            performanceLogger.error("EXTERNAL_API_ERROR: {} - {}ms - {}", 
                methodName, executionTime, e.getMessage());
            throw e;
        }
    }
}