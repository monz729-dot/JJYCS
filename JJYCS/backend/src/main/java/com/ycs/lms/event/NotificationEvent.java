package com.ycs.lms.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonTypeName("NOTIFICATION_EVENT")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class NotificationEvent extends BaseEvent {
    
    private String recipientId;
    private String recipientEmail;
    private String notificationType; // EMAIL, SMS, PUSH, IN_APP
    private String template;
    private String title;
    private String message;
    private Map<String, Object> templateData;
    private String priority; // HIGH, MEDIUM, LOW
    private boolean sendImmediately;

    public NotificationEvent(String recipientId, String recipientEmail, String notificationType) {
        super("NOTIFICATION_EVENT", "notification-service");
        this.recipientId = recipientId;
        this.recipientEmail = recipientEmail;
        this.notificationType = notificationType;
        this.priority = "MEDIUM";
        this.sendImmediately = false;
    }

    public static NotificationEvent email(String recipientId, String recipientEmail, 
                                        String template, String title, String message) {
        NotificationEvent event = new NotificationEvent(recipientId, recipientEmail, "EMAIL");
        event.setTemplate(template);
        event.setTitle(title);
        event.setMessage(message);
        return event;
    }

    public static NotificationEvent sms(String recipientId, String phone, String message) {
        NotificationEvent event = new NotificationEvent(recipientId, phone, "SMS");
        event.setMessage(message);
        event.setPriority("HIGH");
        return event;
    }

    public static NotificationEvent inApp(String recipientId, String title, String message) {
        NotificationEvent event = new NotificationEvent(recipientId, null, "IN_APP");
        event.setTitle(title);
        event.setMessage(message);
        return event;
    }

    public static NotificationEvent orderStatusChanged(String userId, String email, 
                                                     String orderNumber, String newStatus) {
        NotificationEvent event = email(userId, email, "order-status-changed", 
            "주문 상태 변경 알림", 
            String.format("주문 %s의 상태가 %s로 변경되었습니다.", orderNumber, newStatus));
        
        event.getTemplateData().put("orderNumber", orderNumber);
        event.getTemplateData().put("newStatus", newStatus);
        return event;
    }

    public static NotificationEvent userApproved(String userId, String email, String userType) {
        NotificationEvent event = email(userId, email, "user-approved",
            "계정 승인 완료",
            String.format("%s 계정이 승인되었습니다. 이제 서비스를 이용하실 수 있습니다.", userType));
        
        event.getTemplateData().put("userType", userType);
        event.setPriority("HIGH");
        return event;
    }

    public static NotificationEvent passwordReset(String userId, String email, String resetToken) {
        NotificationEvent event = email(userId, email, "password-reset",
            "비밀번호 재설정 요청",
            "비밀번호 재설정을 요청하셨습니다. 다음 링크를 클릭하여 비밀번호를 재설정해주세요.");
        
        event.getTemplateData().put("resetToken", resetToken);
        event.setPriority("HIGH");
        event.setSendImmediately(true);
        return event;
    }
}