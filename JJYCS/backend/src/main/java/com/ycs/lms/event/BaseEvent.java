package com.ycs.lms.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderEvent.class, name = "ORDER_EVENT"),
    @JsonSubTypes.Type(value = UserEvent.class, name = "USER_EVENT"),
    @JsonSubTypes.Type(value = NotificationEvent.class, name = "NOTIFICATION_EVENT"),
    @JsonSubTypes.Type(value = AuditEvent.class, name = "AUDIT_EVENT"),
    @JsonSubTypes.Type(value = SystemEvent.class, name = "SYSTEM_EVENT")
})
@Data
@NoArgsConstructor
public abstract class BaseEvent {
    
    protected String eventId;
    protected String eventType;
    protected String source;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime timestamp;
    
    protected String correlationId;
    protected String userId;
    protected Long version;

    protected BaseEvent(String eventType, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.source = source;
        this.timestamp = LocalDateTime.now();
        this.version = 1L;
    }
}