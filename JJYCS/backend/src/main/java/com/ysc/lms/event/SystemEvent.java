package com.ysc.lms.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonTypeName("SYSTEM_EVENT")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SystemEvent extends BaseEvent {
    
    private String component;
    private String action; // STARTUP, SHUTDOWN, HEALTH_CHECK, BACKUP_CREATED, CACHE_CLEARED
    private String status; // SUCCESS, FAILED, WARNING
    private String message;
    private Map<String, Object> metadata;

    public SystemEvent(String component, String action, String status) {
        super("SYSTEM_EVENT", "system");
        this.component = component;
        this.action = action;
        this.status = status;
    }

    public static SystemEvent startup(String component) {
        SystemEvent event = new SystemEvent(component, "STARTUP", "SUCCESS");
        event.setMessage(String.format("%s 컴포넌트가 시작되었습니다.", component));
        return event;
    }

    public static SystemEvent shutdown(String component) {
        SystemEvent event = new SystemEvent(component, "SHUTDOWN", "SUCCESS");
        event.setMessage(String.format("%s 컴포넌트가 종료되었습니다.", component));
        return event;
    }

    public static SystemEvent healthCheckFailed(String component, String reason) {
        SystemEvent event = new SystemEvent(component, "HEALTH_CHECK", "FAILED");
        event.setMessage(String.format("%s 헬스체크 실패: %s", component, reason));
        return event;
    }

    public static SystemEvent backupCreated(String backupPath, long fileSize) {
        SystemEvent event = new SystemEvent("backup-service", "BACKUP_CREATED", "SUCCESS");
        event.setMessage("데이터베이스 백업이 생성되었습니다.");
        event.getMetadata().put("backupPath", backupPath);
        event.getMetadata().put("fileSize", fileSize);
        return event;
    }

    public static SystemEvent cacheCleared(String cacheName) {
        SystemEvent event = new SystemEvent("cache-service", "CACHE_CLEARED", "SUCCESS");
        event.setMessage(String.format("%s 캐시가 삭제되었습니다.", cacheName));
        event.getMetadata().put("cacheName", cacheName);
        return event;
    }

    public static SystemEvent externalApiError(String apiName, String errorMessage) {
        SystemEvent event = new SystemEvent("external-api", "API_ERROR", "FAILED");
        event.setMessage(String.format("%s API 호출 실패: %s", apiName, errorMessage));
        event.getMetadata().put("apiName", apiName);
        event.getMetadata().put("errorMessage", errorMessage);
        return event;
    }
}