package com.ycs.lms.compliance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceService {

    private final List<AuditTrail> auditTrails = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, ComplianceRule> complianceRules = new ConcurrentHashMap<>();
    private final List<ComplianceViolation> violations = Collections.synchronizedList(new ArrayList<>());

    public void initializeComplianceRules() {
        log.info("Initializing compliance rules");
        
        // GDPR Compliance Rules
        addRule(ComplianceRule.builder()
            .ruleId("GDPR_DATA_RETENTION")
            .name("GDPR 데이터 보존 기간")
            .description("개인정보는 수집 목적 달성 후 지체없이 삭제되어야 함")
            .regulation("GDPR")
            .severity(ComplianceSeverity.HIGH)
            .category(ComplianceCategory.DATA_PROTECTION)
            .build());
            
        addRule(ComplianceRule.builder()
            .ruleId("GDPR_CONSENT_TRACKING")
            .name("GDPR 동의 추적")
            .description("개인정보 처리에 대한 명시적 동의가 기록되어야 함")
            .regulation("GDPR")
            .severity(ComplianceSeverity.CRITICAL)
            .category(ComplianceCategory.CONSENT_MANAGEMENT)
            .build());
            
        // SOX Compliance Rules
        addRule(ComplianceRule.builder()
            .ruleId("SOX_FINANCIAL_CONTROLS")
            .name("SOX 재무 통제")
            .description("재무 데이터 변경사항이 모두 감사 추적되어야 함")
            .regulation("SOX")
            .severity(ComplianceSeverity.CRITICAL)
            .category(ComplianceCategory.FINANCIAL_CONTROLS)
            .build());
            
        // ISO 27001 Compliance Rules
        addRule(ComplianceRule.builder()
            .ruleId("ISO27001_ACCESS_CONTROL")
            .name("ISO27001 접근 제어")
            .description("시스템 접근이 인가된 사용자에게만 허용되어야 함")
            .regulation("ISO27001")
            .severity(ComplianceSeverity.HIGH)
            .category(ComplianceCategory.ACCESS_CONTROL)
            .build());
    }

    public void recordAuditTrail(AuditTrail auditTrail) {
        auditTrail.setTimestamp(LocalDateTime.now());
        auditTrail.setTenantId(getCurrentTenantId());
        auditTrails.add(auditTrail);
        
        log.debug("Recorded audit trail: {} by {}", auditTrail.getAction(), auditTrail.getUserId());
        
        // Check compliance rules
        checkCompliance(auditTrail);
        
        // Clean old audit trails periodically
        if (auditTrails.size() % 1000 == 0) {
            cleanOldAuditTrails();
        }
    }

    public void checkCompliance(AuditTrail auditTrail) {
        for (ComplianceRule rule : complianceRules.values()) {
            if (isRuleViolated(rule, auditTrail)) {
                recordViolation(rule, auditTrail);
            }
        }
    }

    public ComplianceReport generateComplianceReport(String regulation, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating compliance report for {} from {} to {}", regulation, startDate, endDate);
        
        List<AuditTrail> relevantTrails = auditTrails.stream()
            .filter(trail -> trail.getTimestamp().isAfter(startDate) && trail.getTimestamp().isBefore(endDate))
            .collect(Collectors.toList());
            
        List<ComplianceViolation> relevantViolations = violations.stream()
            .filter(violation -> violation.getDetectedAt().isAfter(startDate) && violation.getDetectedAt().isBefore(endDate))
            .filter(violation -> regulation == null || regulation.equals(violation.getRegulation()))
            .collect(Collectors.toList());
            
        return ComplianceReport.builder()
            .regulation(regulation)
            .reportPeriodStart(startDate)
            .reportPeriodEnd(endDate)
            .totalAuditTrails(relevantTrails.size())
            .totalViolations(relevantViolations.size())
            .violations(relevantViolations)
            .complianceScore(calculateComplianceScore(relevantTrails, relevantViolations))
            .recommendations(generateRecommendations(relevantViolations))
            .generatedAt(LocalDateTime.now())
            .build();
    }

    public List<AuditTrail> getAuditTrails(String userId, String action, LocalDateTime startDate, LocalDateTime endDate, int limit) {
        return auditTrails.stream()
            .filter(trail -> userId == null || userId.equals(trail.getUserId()))
            .filter(trail -> action == null || action.equals(trail.getAction()))
            .filter(trail -> startDate == null || trail.getTimestamp().isAfter(startDate))
            .filter(trail -> endDate == null || trail.getTimestamp().isBefore(endDate))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    public List<ComplianceViolation> getViolations(String regulation, ComplianceSeverity severity, LocalDateTime startDate, LocalDateTime endDate) {
        return violations.stream()
            .filter(violation -> regulation == null || regulation.equals(violation.getRegulation()))
            .filter(violation -> severity == null || severity.equals(violation.getSeverity()))
            .filter(violation -> startDate == null || violation.getDetectedAt().isAfter(startDate))
            .filter(violation -> endDate == null || violation.getDetectedAt().isBefore(endDate))
            .sorted((a, b) -> b.getDetectedAt().compareTo(a.getDetectedAt()))
            .collect(Collectors.toList());
    }

    public DataPrivacyReport generateDataPrivacyReport(String userId) {
        log.info("Generating data privacy report for user: {}", userId);
        
        List<AuditTrail> userTrails = auditTrails.stream()
            .filter(trail -> userId.equals(trail.getUserId()) || userId.equals(trail.getTargetId()))
            .collect(Collectors.toList());
            
        Map<String, Long> dataProcessingActivities = userTrails.stream()
            .collect(Collectors.groupingBy(
                AuditTrail::getAction,
                Collectors.counting()
            ));
            
        return DataPrivacyReport.builder()
            .userId(userId)
            .totalDataProcessingActivities(userTrails.size())
            .dataProcessingBreakdown(dataProcessingActivities)
            .lastDataAccess(userTrails.stream()
                .map(AuditTrail::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(null))
            .consentStatus("GRANTED") // Mock - would check actual consent records
            .dataRetentionCompliance(checkDataRetentionCompliance(userId))
            .generatedAt(LocalDateTime.now())
            .build();
    }

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void performComplianceCheck() {
        log.info("Starting scheduled compliance check");
        
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime today = LocalDateTime.now();
        
        for (ComplianceRule rule : complianceRules.values()) {
            performRuleCheck(rule, yesterday, today);
        }
        
        log.info("Completed scheduled compliance check");
    }

    private void addRule(ComplianceRule rule) {
        complianceRules.put(rule.getRuleId(), rule);
        log.debug("Added compliance rule: {}", rule.getRuleId());
    }

    private boolean isRuleViolated(ComplianceRule rule, AuditTrail auditTrail) {
        // Simple rule violation logic - would be more sophisticated in real implementation
        switch (rule.getRuleId()) {
            case "GDPR_DATA_RETENTION":
                return checkDataRetentionViolation(auditTrail);
            case "GDPR_CONSENT_TRACKING":
                return checkConsentTrackingViolation(auditTrail);
            case "SOX_FINANCIAL_CONTROLS":
                return checkFinancialControlsViolation(auditTrail);
            case "ISO27001_ACCESS_CONTROL":
                return checkAccessControlViolation(auditTrail);
            default:
                return false;
        }
    }

    private void recordViolation(ComplianceRule rule, AuditTrail auditTrail) {
        ComplianceViolation violation = ComplianceViolation.builder()
            .violationId(UUID.randomUUID().toString())
            .ruleId(rule.getRuleId())
            .ruleName(rule.getName())
            .regulation(rule.getRegulation())
            .severity(rule.getSeverity())
            .category(rule.getCategory())
            .description(rule.getDescription())
            .userId(auditTrail.getUserId())
            .action(auditTrail.getAction())
            .resourceType(auditTrail.getResourceType())
            .resourceId(auditTrail.getResourceId())
            .detectedAt(LocalDateTime.now())
            .status(ViolationStatus.OPEN)
            .tenantId(auditTrail.getTenantId())
            .build();
            
        violations.add(violation);
        
        log.warn("Compliance violation detected: {} - Rule: {}", violation.getViolationId(), rule.getRuleId());
    }

    private boolean checkDataRetentionViolation(AuditTrail auditTrail) {
        // Check if data is being retained beyond policy limits
        return "DATA_ACCESS".equals(auditTrail.getAction()) && 
               auditTrail.getTimestamp().isBefore(LocalDateTime.now().minusYears(7));
    }

    private boolean checkConsentTrackingViolation(AuditTrail auditTrail) {
        // Check if personal data processing lacks consent tracking
        return "USER_DATA_PROCESSING".equals(auditTrail.getAction()) && 
               auditTrail.getAdditionalData().get("consent_tracked") == null;
    }

    private boolean checkFinancialControlsViolation(AuditTrail auditTrail) {
        // Check if financial data changes lack proper authorization
        return "FINANCIAL_DATA_UPDATE".equals(auditTrail.getAction()) && 
               auditTrail.getAdditionalData().get("approved_by") == null;
    }

    private boolean checkAccessControlViolation(AuditTrail auditTrail) {
        // Check if unauthorized access attempts
        return "LOGIN_FAILED".equals(auditTrail.getAction()) && 
               auditTrail.getAdditionalData().get("consecutive_failures") != null &&
               Integer.parseInt(auditTrail.getAdditionalData().get("consecutive_failures").toString()) > 5;
    }

    private double calculateComplianceScore(List<AuditTrail> auditTrails, List<ComplianceViolation> violations) {
        if (auditTrails.isEmpty()) return 100.0;
        
        double violationRate = (double) violations.size() / auditTrails.size();
        return Math.max(0, 100 - (violationRate * 100));
    }

    private List<String> generateRecommendations(List<ComplianceViolation> violations) {
        List<String> recommendations = new ArrayList<>();
        
        Map<String, Long> violationsByRule = violations.stream()
            .collect(Collectors.groupingBy(
                ComplianceViolation::getRuleId,
                Collectors.counting()
            ));
        
        for (Map.Entry<String, Long> entry : violationsByRule.entrySet()) {
            if (entry.getValue() > 5) {
                recommendations.add("Review and strengthen controls for rule: " + entry.getKey());
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("전반적인 규정 준수 상태가 양호합니다.");
        }
        
        return recommendations;
    }

    private boolean checkDataRetentionCompliance(String userId) {
        // Mock implementation - would check actual data retention policies
        return true;
    }

    private void performRuleCheck(ComplianceRule rule, LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditTrail> relevantTrails = auditTrails.stream()
            .filter(trail -> trail.getTimestamp().isAfter(startDate) && trail.getTimestamp().isBefore(endDate))
            .collect(Collectors.toList());
            
        for (AuditTrail trail : relevantTrails) {
            if (isRuleViolated(rule, trail)) {
                recordViolation(rule, trail);
            }
        }
    }

    private void cleanOldAuditTrails() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(12); // Keep 12 months
        int sizeBefore = auditTrails.size();
        
        auditTrails.removeIf(trail -> trail.getTimestamp().isBefore(cutoff));
        
        int sizeAfter = auditTrails.size();
        if (sizeBefore > sizeAfter) {
            log.info("Cleaned {} old audit trails", sizeBefore - sizeAfter);
        }
    }

    private String getCurrentTenantId() {
        // Would get from TenantContext in real implementation
        return "default";
    }

    // DTO Classes
    @lombok.Data
    @lombok.Builder
    public static class AuditTrail {
        private String trailId;
        private String tenantId;
        private String userId;
        private String userEmail;
        private String action;
        private String resourceType;
        private String resourceId;
        private String targetId;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime timestamp;
        private AuditResult result;
        private String failureReason;
        private Map<String, Object> additionalData;
    }

    @lombok.Data
    @lombok.Builder
    public static class ComplianceRule {
        private String ruleId;
        private String name;
        private String description;
        private String regulation;
        private ComplianceSeverity severity;
        private ComplianceCategory category;
        private boolean enabled;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class ComplianceViolation {
        private String violationId;
        private String ruleId;
        private String ruleName;
        private String regulation;
        private ComplianceSeverity severity;
        private ComplianceCategory category;
        private String description;
        private String userId;
        private String action;
        private String resourceType;
        private String resourceId;
        private LocalDateTime detectedAt;
        private ViolationStatus status;
        private String tenantId;
        private String resolutionNotes;
        private LocalDateTime resolvedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class ComplianceReport {
        private String regulation;
        private LocalDateTime reportPeriodStart;
        private LocalDateTime reportPeriodEnd;
        private long totalAuditTrails;
        private long totalViolations;
        private List<ComplianceViolation> violations;
        private double complianceScore;
        private List<String> recommendations;
        private LocalDateTime generatedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class DataPrivacyReport {
        private String userId;
        private long totalDataProcessingActivities;
        private Map<String, Long> dataProcessingBreakdown;
        private LocalDateTime lastDataAccess;
        private String consentStatus;
        private boolean dataRetentionCompliance;
        private LocalDateTime generatedAt;
    }

    public enum ComplianceSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ComplianceCategory {
        DATA_PROTECTION, ACCESS_CONTROL, FINANCIAL_CONTROLS, CONSENT_MANAGEMENT, AUDIT_LOGGING
    }

    public enum AuditResult {
        SUCCESS, FAILURE, WARNING
    }

    public enum ViolationStatus {
        OPEN, IN_PROGRESS, RESOLVED, FALSE_POSITIVE
    }
}