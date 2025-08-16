package com.ycs.lms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 설정 서비스
 * - 비즈니스 룰 설정값 관리
 * - 시스템 설정 관리
 */
@Service
@Slf4j
public class ConfigService {

    @Value("${app.business.cbm-threshold:29.0}")
    private BigDecimal cbmThreshold;

    @Value("${app.business.thb-amount-threshold:1500.0}")
    private BigDecimal thbAmountThreshold;

    @Value("${app.business.auto-air-conversion:true}")
    private boolean autoAirConversion;

    @Value("${app.business.require-member-code:true}")
    private boolean requireMemberCode;

    /**
     * CBM 임계값 조회 (기본값: 29.0)
     */
    public BigDecimal getCBMThreshold() {
        return cbmThreshold;
    }

    /**
     * THB 금액 임계값 조회 (기본값: 1500.0)
     */
    public BigDecimal getThbAmountThreshold() {
        return thbAmountThreshold;
    }

    /**
     * 자동 항공 전환 여부
     */
    public boolean isAutoAirConversion() {
        return autoAirConversion;
    }

    /**
     * 회원코드 필수 여부
     */
    public boolean isRequireMemberCode() {
        return requireMemberCode;
    }

    /**
     * 설정값 업데이트 (관리자용)
     */
    public void updateCBMThreshold(BigDecimal newThreshold) {
        log.info("Updating CBM threshold from {} to {}", cbmThreshold, newThreshold);
        this.cbmThreshold = newThreshold;
    }

    /**
     * 설정값 업데이트 (관리자용)
     */
    public void updateThbAmountThreshold(BigDecimal newThreshold) {
        log.info("Updating THB amount threshold from {} to {}", thbAmountThreshold, newThreshold);
        this.thbAmountThreshold = newThreshold;
    }
} 