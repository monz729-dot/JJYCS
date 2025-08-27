package com.ycs.lms.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@Slf4j
public class LoggingConfig {

    @EventListener(ApplicationReadyEvent.class)
    public void configureLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // 애플리케이션 로그 설정
        configureApplicationLogger(context);
        
        // 보안 로그 설정
        configureSecurityLogger(context);
        
        // 성능 로그 설정
        configurePerformanceLogger(context);
        
        // 에러 로그 설정
        configureErrorLogger(context);
        
        log.info("로깅 설정이 완료되었습니다.");
    }

    private void configureApplicationLogger(LoggerContext context) {
        // 애플리케이션 로그용 Appender
        RollingFileAppender appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("APPLICATION");
        appender.setFile("logs/application.log");

        // 롤링 정책
        TimeBasedRollingPolicy policy = new TimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern("logs/application.%d{yyyy-MM-dd}.%i.log.gz");
        policy.setMaxHistory(30);
        policy.start();

        // 패턴 설정
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n");
        encoder.start();

        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();

        // 루트 로거에 추가
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
        rootLogger.setLevel(Level.INFO);
    }

    private void configureSecurityLogger(LoggerContext context) {
        // 보안 이벤트 로그용 Appender
        RollingFileAppender appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("SECURITY");
        appender.setFile("logs/security.log");

        TimeBasedRollingPolicy policy = new TimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern("logs/security.%d{yyyy-MM-dd}.log.gz");
        policy.setMaxHistory(90); // 3개월 보관
        policy.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [SECURITY] %msg%n");
        encoder.start();

        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();

        // 보안 로거 설정
        Logger securityLogger = context.getLogger("SECURITY");
        securityLogger.addAppender(appender);
        securityLogger.setLevel(Level.INFO);
        securityLogger.setAdditive(false);
    }

    private void configurePerformanceLogger(LoggerContext context) {
        // 성능 로그용 Appender
        RollingFileAppender appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("PERFORMANCE");
        appender.setFile("logs/performance.log");

        TimeBasedRollingPolicy policy = new TimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern("logs/performance.%d{yyyy-MM-dd}.log.gz");
        policy.setMaxHistory(7); // 1주일 보관
        policy.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [PERFORMANCE] %msg%n");
        encoder.start();

        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();

        // 성능 로거 설정
        Logger performanceLogger = context.getLogger("PERFORMANCE");
        performanceLogger.addAppender(appender);
        performanceLogger.setLevel(Level.INFO);
        performanceLogger.setAdditive(false);
    }

    private void configureErrorLogger(LoggerContext context) {
        // 에러 로그용 Appender
        RollingFileAppender appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("ERROR");
        appender.setFile("logs/error.log");

        TimeBasedRollingPolicy policy = new TimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern("logs/error.%d{yyyy-MM-dd}.log.gz");
        policy.setMaxHistory(30);
        policy.start();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [ERROR] [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n%ex");
        encoder.start();

        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();

        // 에러 로거 설정
        Logger errorLogger = context.getLogger("ERROR");
        errorLogger.addAppender(appender);
        errorLogger.setLevel(Level.ERROR);
        errorLogger.setAdditive(false);
    }
}