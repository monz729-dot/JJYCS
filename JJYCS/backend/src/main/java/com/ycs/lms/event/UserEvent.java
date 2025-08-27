package com.ycs.lms.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonTypeName("USER_EVENT")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserEvent extends BaseEvent {
    
    private Long targetUserId;
    private String email;
    private String action; // REGISTERED, APPROVED, REJECTED, ACTIVATED, DEACTIVATED, DELETED
    private String userType; // GENERAL, ENTERPRISE, PARTNER, WAREHOUSE, ADMIN
    private String previousStatus;
    private String currentStatus;

    public UserEvent(String action, Long targetUserId, String email) {
        super("USER_EVENT", "user-service");
        this.action = action;
        this.targetUserId = targetUserId;
        this.email = email;
    }

    public static UserEvent registered(Long userId, String email, String userType) {
        UserEvent event = new UserEvent("REGISTERED", userId, email);
        event.setUserType(userType);
        event.setCurrentStatus("PENDING_VERIFICATION");
        event.setUserId(userId.toString());
        return event;
    }

    public static UserEvent approved(Long userId, String email, String userType, String approvedBy) {
        UserEvent event = new UserEvent("APPROVED", userId, email);
        event.setUserType(userType);
        event.setPreviousStatus("PENDING_APPROVAL");
        event.setCurrentStatus("ACTIVE");
        event.setUserId(approvedBy);
        return event;
    }

    public static UserEvent rejected(Long userId, String email, String userType, String rejectedBy) {
        UserEvent event = new UserEvent("REJECTED", userId, email);
        event.setUserType(userType);
        event.setPreviousStatus("PENDING_APPROVAL");
        event.setCurrentStatus("REJECTED");
        event.setUserId(rejectedBy);
        return event;
    }

    public static UserEvent activated(Long userId, String email) {
        UserEvent event = new UserEvent("ACTIVATED", userId, email);
        event.setPreviousStatus("INACTIVE");
        event.setCurrentStatus("ACTIVE");
        event.setUserId(userId.toString());
        return event;
    }

    public static UserEvent deactivated(Long userId, String email, String deactivatedBy) {
        UserEvent event = new UserEvent("DEACTIVATED", userId, email);
        event.setPreviousStatus("ACTIVE");
        event.setCurrentStatus("INACTIVE");
        event.setUserId(deactivatedBy);
        return event;
    }
}