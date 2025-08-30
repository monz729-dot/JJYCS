package com.ysc.lms.multitenancy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final Map<String, TenantConfiguration> tenantConfigs = new ConcurrentHashMap<>();

    public void initializeDefaultTenants() {
        log.info("Initializing default tenants");
        
        // Default tenant for single-tenant mode
        createTenant(TenantConfiguration.builder()
            .tenantId("default")
            .name("Default Tenant")
            .status(TenantStatus.ACTIVE)
            .subscriptionPlan("ENTERPRISE")
            .maxUsers(1000)
            .maxOrders(10000)
            .features(getDefaultFeatures())
            .createdAt(LocalDateTime.now())
            .build());
        
        // Demo tenant for testing
        createTenant(TenantConfiguration.builder()
            .tenantId("demo")
            .name("Demo Corporation")
            .status(TenantStatus.ACTIVE)
            .subscriptionPlan("STANDARD")
            .maxUsers(50)
            .maxOrders(1000)
            .features(getStandardFeatures())
            .createdAt(LocalDateTime.now())
            .build());
            
        // YCS internal tenant
        createTenant(TenantConfiguration.builder()
            .tenantId("ycs")
            .name("YCS Logistics")
            .status(TenantStatus.ACTIVE)
            .subscriptionPlan("PREMIUM")
            .maxUsers(500)
            .maxOrders(50000)
            .features(getPremiumFeatures())
            .createdAt(LocalDateTime.now())
            .build());
    }

    public void createTenant(TenantConfiguration config) {
        log.info("Creating tenant: {} ({})", config.getTenantId(), config.getName());
        
        validateTenantConfig(config);
        tenantConfigs.put(config.getTenantId(), config);
        
        // Initialize tenant-specific resources
        initializeTenantResources(config);
    }

    @Cacheable(value = "tenants", key = "#tenantId")
    public TenantConfiguration getTenantConfig(String tenantId) {
        TenantConfiguration config = tenantConfigs.get(tenantId);
        if (config == null) {
            log.warn("Tenant configuration not found for: {}", tenantId);
            return getDefaultTenantConfig();
        }
        return config;
    }

    public boolean isTenantActive(String tenantId) {
        TenantConfiguration config = getTenantConfig(tenantId);
        return config.getStatus() == TenantStatus.ACTIVE;
    }

    public boolean hasFeature(String tenantId, String feature) {
        TenantConfiguration config = getTenantConfig(tenantId);
        return config.getFeatures().contains(feature);
    }

    public boolean canCreateUser(String tenantId) {
        TenantConfiguration config = getTenantConfig(tenantId);
        int currentUsers = getCurrentUserCount(tenantId);
        return currentUsers < config.getMaxUsers();
    }

    public boolean canCreateOrder(String tenantId) {
        TenantConfiguration config = getTenantConfig(tenantId);
        int currentOrders = getCurrentOrderCount(tenantId);
        return currentOrders < config.getMaxOrders();
    }

    public void updateTenant(String tenantId, TenantConfiguration updatedConfig) {
        log.info("Updating tenant configuration for: {}", tenantId);
        
        TenantConfiguration existing = tenantConfigs.get(tenantId);
        if (existing == null) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }
        
        updatedConfig.setTenantId(tenantId);
        updatedConfig.setCreatedAt(existing.getCreatedAt());
        updatedConfig.setUpdatedAt(LocalDateTime.now());
        
        tenantConfigs.put(tenantId, updatedConfig);
    }

    public void deactivateTenant(String tenantId, String reason) {
        log.warn("Deactivating tenant: {} - Reason: {}", tenantId, reason);
        
        TenantConfiguration config = tenantConfigs.get(tenantId);
        if (config != null) {
            config.setStatus(TenantStatus.SUSPENDED);
            config.setUpdatedAt(LocalDateTime.now());
        }
    }

    public List<TenantConfiguration> getAllTenants() {
        return new ArrayList<>(tenantConfigs.values());
    }

    public List<TenantConfiguration> getActiveTenants() {
        return tenantConfigs.values().stream()
            .filter(config -> config.getStatus() == TenantStatus.ACTIVE)
            .toList();
    }

    public TenantStatistics getTenantStatistics(String tenantId) {
        TenantConfiguration config = getTenantConfig(tenantId);
        
        return TenantStatistics.builder()
            .tenantId(tenantId)
            .name(config.getName())
            .status(config.getStatus())
            .currentUsers(getCurrentUserCount(tenantId))
            .maxUsers(config.getMaxUsers())
            .currentOrders(getCurrentOrderCount(tenantId))
            .maxOrders(config.getMaxOrders())
            .storageUsed(getStorageUsed(tenantId))
            .lastActivity(getLastActivity(tenantId))
            .build();
    }

    private void validateTenantConfig(TenantConfiguration config) {
        if (config.getTenantId() == null || config.getTenantId().trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID is required");
        }
        
        if (config.getName() == null || config.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant name is required");
        }
        
        if (tenantConfigs.containsKey(config.getTenantId())) {
            throw new IllegalArgumentException("Tenant already exists: " + config.getTenantId());
        }
        
        // Validate subscription plan
        List<String> validPlans = Arrays.asList("BASIC", "STANDARD", "PREMIUM", "ENTERPRISE");
        if (!validPlans.contains(config.getSubscriptionPlan())) {
            throw new IllegalArgumentException("Invalid subscription plan: " + config.getSubscriptionPlan());
        }
    }

    private void initializeTenantResources(TenantConfiguration config) {
        // Initialize tenant-specific database schema, indexes, etc.
        log.debug("Initializing resources for tenant: {}", config.getTenantId());
        
        // This would typically involve:
        // 1. Creating tenant-specific database schemas
        // 2. Setting up tenant-specific cache namespaces
        // 3. Initializing tenant-specific configuration
        // 4. Creating default admin user for the tenant
    }

    private TenantConfiguration getDefaultTenantConfig() {
        return TenantConfiguration.builder()
            .tenantId("default")
            .name("Default Tenant")
            .status(TenantStatus.ACTIVE)
            .subscriptionPlan("BASIC")
            .maxUsers(100)
            .maxOrders(1000)
            .features(getBasicFeatures())
            .createdAt(LocalDateTime.now())
            .build();
    }

    private Set<String> getBasicFeatures() {
        return Set.of(
            "ORDER_MANAGEMENT",
            "USER_MANAGEMENT",
            "BASIC_REPORTING"
        );
    }

    private Set<String> getStandardFeatures() {
        return Set.of(
            "ORDER_MANAGEMENT",
            "USER_MANAGEMENT", 
            "BASIC_REPORTING",
            "ADVANCED_SEARCH",
            "EMAIL_NOTIFICATIONS"
        );
    }

    private Set<String> getPremiumFeatures() {
        return Set.of(
            "ORDER_MANAGEMENT",
            "USER_MANAGEMENT",
            "BASIC_REPORTING",
            "ADVANCED_SEARCH",
            "EMAIL_NOTIFICATIONS",
            "API_ACCESS",
            "CUSTOM_WORKFLOWS",
            "ADVANCED_ANALYTICS"
        );
    }

    private Set<String> getDefaultFeatures() {
        return Set.of(
            "ORDER_MANAGEMENT",
            "USER_MANAGEMENT",
            "BASIC_REPORTING",
            "ADVANCED_SEARCH",
            "EMAIL_NOTIFICATIONS",
            "API_ACCESS",
            "CUSTOM_WORKFLOWS",
            "ADVANCED_ANALYTICS",
            "MACHINE_LEARNING",
            "UNLIMITED_STORAGE"
        );
    }

    private int getCurrentUserCount(String tenantId) {
        // Mock implementation - would query actual user count
        return (int) (Math.random() * 50);
    }

    private int getCurrentOrderCount(String tenantId) {
        // Mock implementation - would query actual order count
        return (int) (Math.random() * 500);
    }

    private long getStorageUsed(String tenantId) {
        // Mock implementation - would calculate actual storage usage
        return (long) (Math.random() * 1024 * 1024 * 100); // Random MB
    }

    private LocalDateTime getLastActivity(String tenantId) {
        // Mock implementation - would get actual last activity
        return LocalDateTime.now().minusHours((long) (Math.random() * 24));
    }

    // DTO Classes
    @lombok.Data
    @lombok.Builder
    public static class TenantConfiguration {
        private String tenantId;
        private String name;
        private String description;
        private TenantStatus status;
        private String subscriptionPlan;
        private int maxUsers;
        private int maxOrders;
        private long maxStorageBytes;
        private Set<String> features;
        private Map<String, Object> settings;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String contactEmail;
        private String billingEmail;
    }

    @lombok.Data
    @lombok.Builder
    public static class TenantStatistics {
        private String tenantId;
        private String name;
        private TenantStatus status;
        private int currentUsers;
        private int maxUsers;
        private int currentOrders;
        private int maxOrders;
        private long storageUsed;
        private LocalDateTime lastActivity;
        private double userUtilization;
        private double orderUtilization;
    }

    public enum TenantStatus {
        ACTIVE,
        SUSPENDED,
        TRIAL,
        EXPIRED,
        CANCELLED
    }
}