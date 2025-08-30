package com.ysc.lms.rules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessRuleEngine {

    private final Map<String, BusinessRule> rules = new ConcurrentHashMap<>();
    private final Map<String, RuleSet> ruleSets = new ConcurrentHashMap<>();
    private final List<RuleExecutionLog> executionLogs = Collections.synchronizedList(new ArrayList<>());

    public void initializeDefaultRules() {
        log.info("Initializing default business rules");
        
        // Order Processing Rules
        createOrderProcessingRules();
        
        // User Registration Rules
        createUserRegistrationRules();
        
        // Pricing Rules
        createPricingRules();
        
        // Shipping Rules
        createShippingRules();
        
        // Inventory Rules
        createInventoryRules();
        
        // Create rule sets
        createDefaultRuleSets();
    }

    public RuleExecutionResult executeRule(String ruleId, RuleContext context) {
        BusinessRule rule = rules.get(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("Rule not found: " + ruleId);
        }
        
        return executeRule(rule, context);
    }

    public RuleSetExecutionResult executeRuleSet(String ruleSetId, RuleContext context) {
        RuleSet ruleSet = ruleSets.get(ruleSetId);
        if (ruleSet == null) {
            throw new IllegalArgumentException("RuleSet not found: " + ruleSetId);
        }
        
        log.debug("Executing rule set: {} with {} rules", ruleSetId, ruleSet.getRuleIds().size());
        
        List<RuleExecutionResult> results = new ArrayList<>();
        boolean allPassed = true;
        boolean stopOnFirstFailure = ruleSet.getExecutionMode() == ExecutionMode.STOP_ON_FIRST_FAILURE;
        
        for (String ruleId : ruleSet.getRuleIds()) {
            BusinessRule rule = rules.get(ruleId);
            if (rule == null) {
                log.warn("Rule not found in rule set: {}", ruleId);
                continue;
            }
            
            RuleExecutionResult result = executeRule(rule, context);
            results.add(result);
            
            if (!result.isPassed()) {
                allPassed = false;
                if (stopOnFirstFailure) {
                    break;
                }
            }
        }
        
        return RuleSetExecutionResult.builder()
            .ruleSetId(ruleSetId)
            .executionTime(LocalDateTime.now())
            .passed(allPassed)
            .results(results)
            .build();
    }

    public List<RuleExecutionResult> executeRulesForEntity(String entityType, String entityId, Map<String, Object> entityData) {
        RuleContext context = RuleContext.builder()
            .entityType(entityType)
            .entityId(entityId)
            .data(entityData)
            .tenantId(getCurrentTenantId())
            .executionTime(LocalDateTime.now())
            .build();
        
        List<RuleExecutionResult> results = new ArrayList<>();
        
        // Find applicable rules for the entity type
        List<BusinessRule> applicableRules = rules.values().stream()
            .filter(rule -> rule.getApplicableEntities().contains(entityType))
            .filter(rule -> rule.isActive())
            .sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
            .collect(Collectors.toList());
        
        for (BusinessRule rule : applicableRules) {
            RuleExecutionResult result = executeRule(rule, context);
            results.add(result);
            
            // Apply rule actions if rule passed
            if (result.isPassed()) {
                applyRuleActions(rule, context, result);
            }
        }
        
        return results;
    }

    private RuleExecutionResult executeRule(BusinessRule rule, RuleContext context) {
        log.debug("Executing rule: {} for entity: {}", rule.getRuleId(), context.getEntityId());
        
        long startTime = System.currentTimeMillis();
        
        try {
            boolean conditionResult = evaluateCondition(rule.getCondition(), context);
            long executionTime = System.currentTimeMillis() - startTime;
            
            RuleExecutionResult result = RuleExecutionResult.builder()
                .ruleId(rule.getRuleId())
                .ruleName(rule.getName())
                .entityType(context.getEntityType())
                .entityId(context.getEntityId())
                .tenantId(context.getTenantId())
                .passed(conditionResult)
                .executionTimeMs(executionTime)
                .executedAt(LocalDateTime.now())
                .message(conditionResult ? rule.getSuccessMessage() : rule.getFailureMessage())
                .severity(rule.getSeverity())
                .build();
            
            // Log execution
            logRuleExecution(rule, context, result);
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.error("Rule execution failed: {} for entity: {}", rule.getRuleId(), context.getEntityId(), e);
            
            return RuleExecutionResult.builder()
                .ruleId(rule.getRuleId())
                .ruleName(rule.getName())
                .entityType(context.getEntityType())
                .entityId(context.getEntityId())
                .tenantId(context.getTenantId())
                .passed(false)
                .executionTimeMs(executionTime)
                .executedAt(LocalDateTime.now())
                .message("Rule execution error: " + e.getMessage())
                .error(e.getMessage())
                .severity(RuleSeverity.ERROR)
                .build();
        }
    }

    private boolean evaluateCondition(RuleCondition condition, RuleContext context) {
        switch (condition.getType()) {
            case SIMPLE:
                return evaluateSimpleCondition(condition, context);
            case COMPOSITE:
                return evaluateCompositeCondition(condition, context);
            case SCRIPT:
                return evaluateScriptCondition(condition, context);
            default:
                throw new UnsupportedOperationException("Unsupported condition type: " + condition.getType());
        }
    }

    private boolean evaluateSimpleCondition(RuleCondition condition, RuleContext context) {
        String field = condition.getField();
        String operator = condition.getOperator();
        Object expectedValue = condition.getValue();
        
        Object actualValue = getValueFromContext(field, context);
        
        return compareValues(actualValue, expectedValue, operator);
    }

    private boolean evaluateCompositeCondition(RuleCondition condition, RuleContext context) {
        List<RuleCondition> subConditions = condition.getSubConditions();
        String logicalOperator = condition.getLogicalOperator();
        
        if ("AND".equals(logicalOperator)) {
            return subConditions.stream().allMatch(subCondition -> evaluateCondition(subCondition, context));
        } else if ("OR".equals(logicalOperator)) {
            return subConditions.stream().anyMatch(subCondition -> evaluateCondition(subCondition, context));
        } else {
            throw new UnsupportedOperationException("Unsupported logical operator: " + logicalOperator);
        }
    }

    private boolean evaluateScriptCondition(RuleCondition condition, RuleContext context) {
        // Simple script evaluation - in real implementation would use scripting engine like Groovy or JavaScript
        String script = condition.getScript();
        return executeScript(script, context);
    }

    private Object getValueFromContext(String field, RuleContext context) {
        if (field.startsWith("data.")) {
            String dataField = field.substring(5);
            return context.getData().get(dataField);
        } else if (field.startsWith("context.")) {
            String contextField = field.substring(8);
            switch (contextField) {
                case "entityType":
                    return context.getEntityType();
                case "entityId":
                    return context.getEntityId();
                case "tenantId":
                    return context.getTenantId();
                default:
                    return null;
            }
        }
        return null;
    }

    private boolean compareValues(Object actual, Object expected, String operator) {
        if (actual == null && expected == null) {
            return "EQUALS".equals(operator);
        }
        
        if (actual == null || expected == null) {
            return "NOT_EQUALS".equals(operator);
        }
        
        switch (operator) {
            case "EQUALS":
                return actual.equals(expected);
            case "NOT_EQUALS":
                return !actual.equals(expected);
            case "GREATER_THAN":
                return compareNumeric(actual, expected) > 0;
            case "GREATER_THAN_OR_EQUAL":
                return compareNumeric(actual, expected) >= 0;
            case "LESS_THAN":
                return compareNumeric(actual, expected) < 0;
            case "LESS_THAN_OR_EQUAL":
                return compareNumeric(actual, expected) <= 0;
            case "CONTAINS":
                return actual.toString().contains(expected.toString());
            case "STARTS_WITH":
                return actual.toString().startsWith(expected.toString());
            case "ENDS_WITH":
                return actual.toString().endsWith(expected.toString());
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + operator);
        }
    }

    private int compareNumeric(Object actual, Object expected) {
        if (actual instanceof Number && expected instanceof Number) {
            Double actualDouble = ((Number) actual).doubleValue();
            Double expectedDouble = ((Number) expected).doubleValue();
            return actualDouble.compareTo(expectedDouble);
        }
        throw new IllegalArgumentException("Cannot compare non-numeric values");
    }

    private boolean executeScript(String script, RuleContext context) {
        // Mock script execution - in real implementation would use scripting engine
        log.debug("Executing script: {}", script);
        
        if (script.contains("cbm > 29")) {
            Double cbm = (Double) context.getData().get("totalCbm");
            return cbm != null && cbm > 29.0;
        }
        
        if (script.contains("totalValue > 1500")) {
            Double totalValue = (Double) context.getData().get("totalValue");
            return totalValue != null && totalValue > 1500.0;
        }
        
        if (script.contains("memberCode == null")) {
            String memberCode = (String) context.getData().get("memberCode");
            return memberCode == null || memberCode.trim().isEmpty();
        }
        
        return false;
    }

    private void applyRuleActions(BusinessRule rule, RuleContext context, RuleExecutionResult result) {
        List<RuleAction> actions = rule.getActions();
        if (actions == null || actions.isEmpty()) {
            return;
        }
        
        for (RuleAction action : actions) {
            try {
                executeRuleAction(action, context, result);
            } catch (Exception e) {
                log.error("Failed to execute rule action: {} for rule: {}", action.getType(), rule.getRuleId(), e);
            }
        }
    }

    private void executeRuleAction(RuleAction action, RuleContext context, RuleExecutionResult result) {
        switch (action.getType()) {
            case UPDATE_FIELD:
                updateFieldAction(action, context);
                break;
            case SEND_NOTIFICATION:
                sendNotificationAction(action, context);
                break;
            case LOG_EVENT:
                logEventAction(action, context);
                break;
            case EXECUTE_SERVICE:
                executeServiceAction(action, context);
                break;
            case SET_FLAG:
                setFlagAction(action, context);
                break;
        }
    }

    private void updateFieldAction(RuleAction action, RuleContext context) {
        String field = action.getParameters().get("field");
        Object value = action.getParameters().get("value");
        
        context.getData().put(field, value);
        log.debug("Updated field {} to {} for entity {}", field, value, context.getEntityId());
    }

    private void sendNotificationAction(RuleAction action, RuleContext context) {
        String message = action.getParameters().get("message");
        String recipient = action.getParameters().get("recipient");
        
        log.info("Sending notification to {}: {}", recipient, message);
        // In real implementation would send actual notification
    }

    private void logEventAction(RuleAction action, RuleContext context) {
        String eventType = action.getParameters().get("eventType");
        String message = action.getParameters().get("message");
        
        log.info("Business rule event [{}]: {} for entity {}", eventType, message, context.getEntityId());
    }

    private void executeServiceAction(RuleAction action, RuleContext context) {
        String serviceName = action.getParameters().get("serviceName");
        String methodName = action.getParameters().get("methodName");
        
        log.debug("Executing service action: {}.{} for entity {}", serviceName, methodName, context.getEntityId());
        // In real implementation would execute actual service method
    }

    private void setFlagAction(RuleAction action, RuleContext context) {
        String flagName = action.getParameters().get("flagName");
        Boolean flagValue = Boolean.valueOf(action.getParameters().get("flagValue"));
        
        context.getData().put(flagName, flagValue);
        log.debug("Set flag {} to {} for entity {}", flagName, flagValue, context.getEntityId());
    }

    private void logRuleExecution(BusinessRule rule, RuleContext context, RuleExecutionResult result) {
        RuleExecutionLog log = RuleExecutionLog.builder()
            .ruleId(rule.getRuleId())
            .entityType(context.getEntityType())
            .entityId(context.getEntityId())
            .tenantId(context.getTenantId())
            .passed(result.isPassed())
            .executionTimeMs(result.getExecutionTimeMs())
            .executedAt(result.getExecutedAt())
            .message(result.getMessage())
            .build();
        
        executionLogs.add(log);
        
        // Keep only recent logs (last 10000)
        if (executionLogs.size() > 10000) {
            executionLogs.removeIf(l -> l.getExecutedAt().isBefore(LocalDateTime.now().minusDays(7)));
        }
    }

    private void createOrderProcessingRules() {
        // CBM Auto-switch Rule
        addRule(BusinessRule.builder()
            .ruleId("CBM_AUTO_SWITCH")
            .name("CBM 자동 전환 규칙")
            .description("총 CBM이 29를 초과하면 자동으로 항공 배송으로 전환")
            .applicableEntities(Set.of("ORDER"))
            .priority(100)
            .active(true)
            .condition(RuleCondition.builder()
                .type(ConditionType.SCRIPT)
                .script("cbm > 29")
                .build())
            .actions(List.of(
                RuleAction.builder()
                    .type(ActionType.UPDATE_FIELD)
                    .parameters(Map.of("field", "orderType", "value", "air"))
                    .build(),
                RuleAction.builder()
                    .type(ActionType.SET_FLAG)
                    .parameters(Map.of("flagName", "cbmAutoSwitched", "flagValue", "true"))
                    .build(),
                RuleAction.builder()
                    .type(ActionType.SEND_NOTIFICATION)
                    .parameters(Map.of("recipient", "customer", "message", "CBM 초과로 항공 배송으로 변경되었습니다"))
                    .build()
            ))
            .severity(RuleSeverity.WARNING)
            .successMessage("CBM 자동 전환 규칙 적용")
            .failureMessage("CBM이 정상 범위 내입니다")
            .build());

        // THB Value Check Rule
        addRule(BusinessRule.builder()
            .ruleId("THB_VALUE_CHECK")
            .name("THB 가치 확인 규칙")
            .description("총 가치가 THB 1,500을 초과하면 추가 수취인 정보 필요")
            .applicableEntities(Set.of("ORDER"))
            .priority(200)
            .active(true)
            .condition(RuleCondition.builder()
                .type(ConditionType.SCRIPT)
                .script("totalValue > 1500")
                .build())
            .actions(List.of(
                RuleAction.builder()
                    .type(ActionType.SET_FLAG)
                    .parameters(Map.of("flagName", "requiresExtraRecipient", "flagValue", "true"))
                    .build(),
                RuleAction.builder()
                    .type(ActionType.SEND_NOTIFICATION)
                    .parameters(Map.of("recipient", "customer", "message", "고가 상품으로 추가 수취인 정보가 필요합니다"))
                    .build()
            ))
            .severity(RuleSeverity.INFO)
            .successMessage("고가 상품 규칙 적용")
            .failureMessage("일반 가치 상품")
            .build());

        // Member Code Check Rule
        addRule(BusinessRule.builder()
            .ruleId("MEMBER_CODE_CHECK")
            .name("회원 코드 확인 규칙")
            .description("회원 코드가 없으면 지연 처리")
            .applicableEntities(Set.of("ORDER"))
            .priority(50)
            .active(true)
            .condition(RuleCondition.builder()
                .type(ConditionType.SCRIPT)
                .script("memberCode == null")
                .build())
            .actions(List.of(
                RuleAction.builder()
                    .type(ActionType.UPDATE_FIELD)
                    .parameters(Map.of("field", "status", "value", "DELAYED"))
                    .build(),
                RuleAction.builder()
                    .type(ActionType.SET_FLAG)
                    .parameters(Map.of("flagName", "noMemberCode", "flagValue", "true"))
                    .build(),
                RuleAction.builder()
                    .type(ActionType.LOG_EVENT)
                    .parameters(Map.of("eventType", "MEMBER_CODE_MISSING", "message", "주문에 회원 코드가 없어 지연 처리됩니다"))
                    .build()
            ))
            .severity(RuleSeverity.WARNING)
            .successMessage("회원 코드 누락 - 지연 처리")
            .failureMessage("회원 코드 확인됨")
            .build());
    }

    private void createUserRegistrationRules() {
        // Email Domain Validation
        addRule(BusinessRule.builder()
            .ruleId("EMAIL_DOMAIN_VALIDATION")
            .name("이메일 도메인 검증")
            .description("기업 이메일 도메인 검증")
            .applicableEntities(Set.of("USER"))
            .priority(100)
            .active(true)
            .condition(RuleCondition.builder()
                .type(ConditionType.SIMPLE)
                .field("data.email")
                .operator("ENDS_WITH")
                .value(".com")
                .build())
            .severity(RuleSeverity.INFO)
            .successMessage("유효한 이메일 도메인")
            .failureMessage("유효하지 않은 이메일 도메인")
            .build());

        // Company Registration Check
        addRule(BusinessRule.builder()
            .ruleId("COMPANY_REGISTRATION_CHECK")
            .name("회사 등록 확인")
            .description("기업 사용자의 회사 등록번호 필수")
            .applicableEntities(Set.of("USER"))
            .priority(200)
            .active(true)
            .condition(RuleCondition.builder()
                .type(ConditionType.COMPOSITE)
                .logicalOperator("AND")
                .subConditions(List.of(
                    RuleCondition.builder()
                        .type(ConditionType.SIMPLE)
                        .field("data.userType")
                        .operator("EQUALS")
                        .value("ENTERPRISE")
                        .build(),
                    RuleCondition.builder()
                        .type(ConditionType.SIMPLE)
                        .field("data.companyRegistrationNumber")
                        .operator("NOT_EQUALS")
                        .value(null)
                        .build()
                ))
                .build())
            .severity(RuleSeverity.ERROR)
            .successMessage("회사 등록번호 확인됨")
            .failureMessage("기업 사용자는 회사 등록번호가 필수입니다")
            .build());
    }

    private void createPricingRules() {
        // Volume Discount Rule
        addRule(BusinessRule.builder()
            .ruleId("VOLUME_DISCOUNT")
            .name("물량 할인 규칙")
            .description("대량 주문 시 할인 적용")
            .applicableEntities(Set.of("ORDER"))
            .priority(300)
            .active(true)
            .condition(RuleCondition.builder()
                .type(ConditionType.SIMPLE)
                .field("data.totalCbm")
                .operator("GREATER_THAN")
                .value(10.0)
                .build())
            .actions(List.of(
                RuleAction.builder()
                    .type(ActionType.SET_FLAG)
                    .parameters(Map.of("flagName", "volumeDiscount", "flagValue", "true"))
                    .build(),
                RuleAction.builder()
                    .type(ActionType.UPDATE_FIELD)
                    .parameters(Map.of("field", "discountRate", "value", "0.1"))
                    .build()
            ))
            .severity(RuleSeverity.INFO)
            .successMessage("대량 주문 할인 적용")
            .failureMessage("일반 주문")
            .build());
    }

    private void createShippingRules() {
        // Express Shipping Rule
        addRule(BusinessRule.builder()
            .ruleId("EXPRESS_SHIPPING")
            .name("특급 배송 규칙")
            .description("긴급 주문의 특급 배송 처리")
            .applicableEntities(Set.of("ORDER"))
            .priority(50)
            .active(true)
            .condition(RuleCondition.builder()
                .type(ConditionType.SIMPLE)
                .field("data.urgent")
                .operator("EQUALS")
                .value(true)
                .build())
            .actions(List.of(
                RuleAction.builder()
                    .type(ActionType.UPDATE_FIELD)
                    .parameters(Map.of("field", "shippingMethod", "value", "EXPRESS"))
                    .build(),
                RuleAction.builder()
                    .type(ActionType.EXECUTE_SERVICE)
                    .parameters(Map.of("serviceName", "NotificationService", "methodName", "sendUrgentOrderAlert"))
                    .build()
            ))
            .severity(RuleSeverity.HIGH)
            .successMessage("특급 배송으로 처리")
            .failureMessage("일반 배송")
            .build());
    }

    private void createInventoryRules() {
        // Low Stock Alert Rule
        addRule(BusinessRule.builder()
            .ruleId("LOW_STOCK_ALERT")
            .name("재고 부족 알림")
            .description("재고가 부족할 때 알림")
            .applicableEntities(Set.of("INVENTORY"))
            .priority(100)
            .active(true)
            .condition(RuleCondition.builder()
                .type(ConditionType.SIMPLE)
                .field("data.currentStock")
                .operator("LESS_THAN")
                .value(10)
                .build())
            .actions(List.of(
                RuleAction.builder()
                    .type(ActionType.SEND_NOTIFICATION)
                    .parameters(Map.of("recipient", "warehouse_manager", "message", "재고가 부족합니다"))
                    .build(),
                RuleAction.builder()
                    .type(ActionType.LOG_EVENT)
                    .parameters(Map.of("eventType", "LOW_STOCK", "message", "재고 부족 알림"))
                    .build()
            ))
            .severity(RuleSeverity.WARNING)
            .successMessage("재고 부족 알림 발송")
            .failureMessage("재고 충분")
            .build());
    }

    private void createDefaultRuleSets() {
        // Order Processing Rule Set
        createRuleSet(RuleSet.builder()
            .ruleSetId("ORDER_PROCESSING_RULES")
            .name("주문 처리 규칙 세트")
            .description("주문 생성 시 적용되는 모든 규칙")
            .ruleIds(List.of("MEMBER_CODE_CHECK", "CBM_AUTO_SWITCH", "THB_VALUE_CHECK", "VOLUME_DISCOUNT", "EXPRESS_SHIPPING"))
            .executionMode(ExecutionMode.EXECUTE_ALL)
            .active(true)
            .build());

        // User Registration Rule Set
        createRuleSet(RuleSet.builder()
            .ruleSetId("USER_REGISTRATION_RULES")
            .name("사용자 등록 규칙 세트")
            .description("사용자 등록 시 적용되는 모든 규칙")
            .ruleIds(List.of("EMAIL_DOMAIN_VALIDATION", "COMPANY_REGISTRATION_CHECK"))
            .executionMode(ExecutionMode.STOP_ON_FIRST_FAILURE)
            .active(true)
            .build());
    }

    private void addRule(BusinessRule rule) {
        rules.put(rule.getRuleId(), rule);
        log.debug("Added business rule: {}", rule.getRuleId());
    }

    private void createRuleSet(RuleSet ruleSet) {
        ruleSets.put(ruleSet.getRuleSetId(), ruleSet);
        log.debug("Created rule set: {} with {} rules", ruleSet.getRuleSetId(), ruleSet.getRuleIds().size());
    }

    private String getCurrentTenantId() {
        // Would get from TenantContext in real implementation
        return "default";
    }

    // DTO Classes
    @lombok.Data
    @lombok.Builder
    public static class BusinessRule {
        private String ruleId;
        private String name;
        private String description;
        private Set<String> applicableEntities;
        private int priority;
        private boolean active;
        private RuleCondition condition;
        private List<RuleAction> actions;
        private RuleSeverity severity;
        private String successMessage;
        private String failureMessage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
    }

    @lombok.Data
    @lombok.Builder
    public static class RuleCondition {
        private ConditionType type;
        private String field;
        private String operator;
        private Object value;
        private String logicalOperator;
        private List<RuleCondition> subConditions;
        private String script;
    }

    @lombok.Data
    @lombok.Builder
    public static class RuleAction {
        private ActionType type;
        private Map<String, String> parameters;
    }

    @lombok.Data
    @lombok.Builder
    public static class RuleContext {
        private String entityType;
        private String entityId;
        private String tenantId;
        private Map<String, Object> data;
        private LocalDateTime executionTime;
    }

    @lombok.Data
    @lombok.Builder
    public static class RuleExecutionResult {
        private String ruleId;
        private String ruleName;
        private String entityType;
        private String entityId;
        private String tenantId;
        private boolean passed;
        private long executionTimeMs;
        private LocalDateTime executedAt;
        private String message;
        private String error;
        private RuleSeverity severity;
    }

    @lombok.Data
    @lombok.Builder
    public static class RuleSet {
        private String ruleSetId;
        private String name;
        private String description;
        private List<String> ruleIds;
        private ExecutionMode executionMode;
        private boolean active;
        private LocalDateTime createdAt;
        private String createdBy;
    }

    @lombok.Data
    @lombok.Builder
    public static class RuleSetExecutionResult {
        private String ruleSetId;
        private LocalDateTime executionTime;
        private boolean passed;
        private List<RuleExecutionResult> results;
    }

    @lombok.Data
    @lombok.Builder
    public static class RuleExecutionLog {
        private String ruleId;
        private String entityType;
        private String entityId;
        private String tenantId;
        private boolean passed;
        private long executionTimeMs;
        private LocalDateTime executedAt;
        private String message;
    }

    public enum ConditionType {
        SIMPLE, COMPOSITE, SCRIPT
    }

    public enum ActionType {
        UPDATE_FIELD, SEND_NOTIFICATION, LOG_EVENT, EXECUTE_SERVICE, SET_FLAG
    }

    public enum RuleSeverity {
        INFO, WARNING, HIGH, CRITICAL, ERROR
    }

    public enum ExecutionMode {
        EXECUTE_ALL, STOP_ON_FIRST_FAILURE
    }
}