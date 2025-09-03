package com.ysc.lms.service;

import com.ysc.lms.entity.SystemConfig;
import com.ysc.lms.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    /**
     * 설정값을 문자열로 조회
     */
    public String getConfigValue(String key) {
        return systemConfigRepository.findByConfigKeyAndIsActive(key, true)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }

    /**
     * 설정값을 문자열로 조회 (기본값 포함)
     */
    public String getConfigValue(String key, String defaultValue) {
        return systemConfigRepository.findByConfigKeyAndIsActive(key, true)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }

    /**
     * 설정값을 Boolean으로 조회
     */
    public Boolean getBooleanConfig(String key) {
        return systemConfigRepository.findByConfigKeyAndIsActive(key, true)
                .map(SystemConfig::getBooleanValue)
                .orElse(null);
    }

    /**
     * 설정값을 Boolean으로 조회 (기본값 포함)
     */
    public Boolean getBooleanConfig(String key, Boolean defaultValue) {
        return systemConfigRepository.findByConfigKeyAndIsActive(key, true)
                .map(SystemConfig::getBooleanValue)
                .orElse(defaultValue);
    }

    /**
     * 설정값을 BigDecimal로 조회
     */
    public BigDecimal getDecimalConfig(String key) {
        return systemConfigRepository.findByConfigKeyAndIsActive(key, true)
                .map(SystemConfig::getDecimalValue)
                .orElse(null);
    }

    /**
     * 설정값을 BigDecimal로 조회 (기본값 포함)
     */
    public BigDecimal getDecimalConfig(String key, BigDecimal defaultValue) {
        return systemConfigRepository.findByConfigKeyAndIsActive(key, true)
                .map(SystemConfig::getDecimalValue)
                .orElse(defaultValue);
    }

    /**
     * 설정값을 Integer로 조회
     */
    public Integer getIntConfig(String key) {
        return systemConfigRepository.findByConfigKeyAndIsActive(key, true)
                .map(SystemConfig::getIntValue)
                .orElse(null);
    }

    /**
     * 설정값을 Integer로 조회 (기본값 포함)
     */
    public Integer getIntConfig(String key, Integer defaultValue) {
        return systemConfigRepository.findByConfigKeyAndIsActive(key, true)
                .map(SystemConfig::getIntValue)
                .orElse(defaultValue);
    }

    /**
     * 카테고리별 설정 조회
     */
    public List<SystemConfig> getConfigsByCategory(String category) {
        return systemConfigRepository.findByCategoryAndIsActive(category, true);
    }

    /**
     * 모든 활성 설정 조회
     */
    public List<SystemConfig> getAllActiveConfigs() {
        return systemConfigRepository.findByIsActive(true);
    }

    /**
     * 설정 생성 또는 업데이트
     */
    @Transactional
    public SystemConfig saveOrUpdateConfig(String key, String value, String updatedBy) {
        Optional<SystemConfig> existing = systemConfigRepository.findByConfigKey(key);
        
        if (existing.isPresent()) {
            SystemConfig config = existing.get();
            config.updateValue(value, updatedBy);
            return systemConfigRepository.save(config);
        } else {
            log.warn("Trying to update non-existent config: {}", key);
            return null;
        }
    }

    /**
     * 새로운 설정 생성
     */
    @Transactional
    public SystemConfig createConfig(SystemConfig config) {
        if (systemConfigRepository.existsByConfigKey(config.getConfigKey())) {
            throw new IllegalArgumentException("Config key already exists: " + config.getConfigKey());
        }
        return systemConfigRepository.save(config);
    }

    /**
     * 설정 삭제 (비활성화)
     */
    @Transactional
    public void deactivateConfig(String key, String updatedBy) {
        systemConfigRepository.findByConfigKey(key)
                .ifPresent(config -> {
                    config.setIsActive(false);
                    config.setUpdatedBy(updatedBy);
                    systemConfigRepository.save(config);
                });
    }

    /**
     * 비즈니스 룰 관련 편의 메서드들
     */
    public BigDecimal getCbmThreshold() {
        return getDecimalConfig(SystemConfig.BusinessRules.CBM_THRESHOLD, new BigDecimal("29.0"));
    }

    public BigDecimal getThbThreshold() {
        return getDecimalConfig(SystemConfig.BusinessRules.THB_THRESHOLD, new BigDecimal("1500.0"));
    }

    public BigDecimal getDefaultCommissionRate() {
        return getDecimalConfig(SystemConfig.BusinessRules.DEFAULT_COMMISSION_RATE, new BigDecimal("5.0"));
    }

    public Boolean isAutoApprovalEnabled() {
        return getBooleanConfig(SystemConfig.BusinessRules.AUTO_APPROVAL_ENABLED, false);
    }

    public Boolean isMaintenanceMode() {
        return getBooleanConfig(SystemConfig.SystemSettings.MAINTENANCE_MODE, false);
    }

    /**
     * 초기 설정 데이터 생성
     */
    @Transactional
    public void initializeDefaultConfigs() {
        createConfigIfNotExists(
            SystemConfig.BusinessRules.CBM_THRESHOLD,
            "29.0",
            SystemConfig.ConfigType.DECIMAL,
            "BUSINESS_RULE",
            "CBM 임계값 (m³) - 초과 시 항공 전환"
        );

        createConfigIfNotExists(
            SystemConfig.BusinessRules.THB_THRESHOLD,
            "1500.0",
            SystemConfig.ConfigType.DECIMAL,
            "BUSINESS_RULE",
            "THB 임계값 - 초과 시 추가 수취인 정보 필요"
        );

        createConfigIfNotExists(
            SystemConfig.BusinessRules.DEFAULT_COMMISSION_RATE,
            "5.0",
            SystemConfig.ConfigType.DECIMAL,
            "BUSINESS_RULE",
            "기본 파트너 수수료율 (%)"
        );

        createConfigIfNotExists(
            SystemConfig.SystemSettings.MAINTENANCE_MODE,
            "false",
            SystemConfig.ConfigType.BOOLEAN,
            "SYSTEM",
            "시스템 점검 모드"
        );

        createConfigIfNotExists(
            SystemConfig.PaymentSettings.KRW_TO_THB_RATE,
            "0.025",
            SystemConfig.ConfigType.DECIMAL,
            "PAYMENT",
            "원화 -> 태국 바트 환율"
        );

        log.info("Default system configurations initialized");
    }

    private void createConfigIfNotExists(String key, String value, SystemConfig.ConfigType type, String category, String description) {
        if (!systemConfigRepository.existsByConfigKey(key)) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType(type);
            config.setCategory(category);
            config.setDescription(description);
            config.setDefaultValue(value);
            config.setDisplayName(description);
            systemConfigRepository.save(config);
        }
    }
}