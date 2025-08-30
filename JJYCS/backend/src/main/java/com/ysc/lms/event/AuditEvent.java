package com.ysc.lms.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonTypeName("AUDIT_EVENT")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AuditEvent extends BaseEvent {
    
    private String action;
    private String entityType;
    private String entityId;
    private String performedBy;
    private String clientIp;
    private String userAgent;
    private String resource;
    private String method;
    private Integer responseStatus;
    private Map<String, Object> requestData;
    private Map<String, Object> responseData;
    private Map<String, Object> beforeState;
    private Map<String, Object> afterState;
    private String description;

    public AuditEvent(String action, String entityType, String performedBy) {
        super("AUDIT_EVENT", "audit-service");
        this.action = action;
        this.entityType = entityType;
        this.performedBy = performedBy;
    }

    public static AuditEvent apiAccess(String performedBy, String clientIp, String resource, 
                                     String method, Integer responseStatus) {
        AuditEvent event = new AuditEvent("API_ACCESS", "API", performedBy);
        event.setClientIp(clientIp);
        event.setResource(resource);
        event.setMethod(method);
        event.setResponseStatus(responseStatus);
        return event;
    }

    public static AuditEvent dataAccess(String performedBy, String entityType, String entityId, 
                                      String action, String description) {
        AuditEvent event = new AuditEvent(action, entityType, performedBy);
        event.setEntityId(entityId);
        event.setDescription(description);
        return event;
    }

    public static AuditEvent securityEvent(String performedBy, String clientIp, String action, 
                                         String description) {
        AuditEvent event = new AuditEvent(action, "SECURITY", performedBy);
        event.setClientIp(clientIp);
        event.setDescription(description);
        return event;
    }

    public static AuditEvent systemEvent(String action, String description) {
        AuditEvent event = new AuditEvent(action, "SYSTEM", "SYSTEM");
        event.setDescription(description);
        return event;
    }
}