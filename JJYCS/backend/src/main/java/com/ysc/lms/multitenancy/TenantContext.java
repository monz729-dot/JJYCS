package com.ysc.lms.multitenancy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContext {
    
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.warn("Attempting to set null or empty tenant ID");
            return;
        }
        currentTenant.set(tenantId);
        log.debug("Set current tenant to: {}", tenantId);
    }
    
    public static String getTenantId() {
        String tenantId = currentTenant.get();
        if (tenantId == null) {
            log.warn("No tenant ID set in current context, using default");
            return "default";
        }
        return tenantId;
    }
    
    public static void clear() {
        String tenantId = currentTenant.get();
        currentTenant.remove();
        log.debug("Cleared tenant context for: {}", tenantId);
    }
    
    public static boolean hasTenant() {
        return currentTenant.get() != null;
    }
}