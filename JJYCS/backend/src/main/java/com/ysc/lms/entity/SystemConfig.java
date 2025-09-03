package com.ysc.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_configs")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey; // 설정 키 (예: cbm.threshold, thb.threshold)

    @Column(nullable = false, length = 50)
    private String category; // 카테고리 (BUSINESS_RULE, SYSTEM, PAYMENT 등)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String configValue; // 설정 값 (JSON 형태 가능)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfigType configType; // 설정 타입

    @Column(length = 200)
    private String displayName; // 화면 표시명

    @Column(length = 500)
    private String description; // 설명

    @Column(length = 100)
    private String defaultValue; // 기본값

    @Column(nullable = false)
    private Boolean isActive = true; // 활성 여부

    @Column(nullable = false)
    private Boolean isReadonly = false; // 읽기 전용 여부

    @Column(length = 200)
    private String validationRule; // 검증 규칙 (정규식 등)

    @Column(length = 100)
    private String updatedBy; // 마지막 수정자

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ConfigType {
        STRING,     // 문자열
        NUMBER,     // 숫자
        BOOLEAN,    // 불린
        JSON,       // JSON 객체
        DECIMAL,    // 소수점
        DATE,       // 날짜
        EMAIL,      // 이메일
        URL         // URL
    }

    // 비즈니스 룰 관련 상수들
    public static class BusinessRules {
        public static final String CBM_THRESHOLD = "cbm.threshold";
        public static final String THB_THRESHOLD = "thb.value.threshold";
        public static final String DEFAULT_COMMISSION_RATE = "partner.commission.rate";
        public static final String DEFAULT_SHIPPING_COST = "shipping.cost.default";
        public static final String AUTO_APPROVAL_ENABLED = "user.auto.approval.enabled";
    }

    // 시스템 설정 관련 상수들
    public static class SystemSettings {
        public static final String MAINTENANCE_MODE = "system.maintenance.mode";
        public static final String MAX_FILE_SIZE = "file.upload.max.size";
        public static final String SESSION_TIMEOUT = "session.timeout.minutes";
        public static final String EMAIL_ENABLED = "email.notification.enabled";
        public static final String SMS_ENABLED = "sms.notification.enabled";
    }

    // 결제 관련 상수들
    public static class PaymentSettings {
        public static final String KRW_TO_THB_RATE = "exchange.rate.krw.to.thb";
        public static final String THB_TO_KRW_RATE = "exchange.rate.thb.to.krw";
        public static final String PAYMENT_GATEWAY = "payment.gateway.default";
    }

    // 값을 타입에 맞게 반환하는 메서드들
    public String getStringValue() {
        return configValue;
    }

    public Boolean getBooleanValue() {
        return configType == ConfigType.BOOLEAN ? Boolean.valueOf(configValue) : null;
    }

    public Integer getIntValue() {
        try {
            return configType == ConfigType.NUMBER ? Integer.valueOf(configValue) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public BigDecimal getDecimalValue() {
        try {
            return (configType == ConfigType.DECIMAL || configType == ConfigType.NUMBER) 
                ? new BigDecimal(configValue) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 설정값 검증
    public boolean isValidValue(String value) {
        if (validationRule == null || validationRule.isEmpty()) {
            return true;
        }
        return value.matches(validationRule);
    }

    // 기본값으로 리셋
    public void resetToDefault() {
        if (defaultValue != null && !isReadonly) {
            this.configValue = defaultValue;
        }
    }

    // 설정값 업데이트
    public void updateValue(String newValue, String updatedBy) {
        if (!isReadonly && isValidValue(newValue)) {
            this.configValue = newValue;
            this.updatedBy = updatedBy;
        }
    }

    // 생성자 - 비즈니스 룰용
    public static SystemConfig createBusinessRule(String key, String value, ConfigType type, String description) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setCategory("BUSINESS_RULE");
        config.setConfigValue(value);
        config.setConfigType(type);
        config.setDescription(description);
        config.setDefaultValue(value);
        return config;
    }

    // 생성자 - 시스템 설정용
    public static SystemConfig createSystemSetting(String key, String value, ConfigType type, String description) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setCategory("SYSTEM");
        config.setConfigValue(value);
        config.setConfigType(type);
        config.setDescription(description);
        config.setDefaultValue(value);
        return config;
    }
}