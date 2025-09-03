package com.ysc.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_templates")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class NotificationTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannelType type;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String smsContent;
    
    @Column(columnDefinition = "TEXT")
    private String variables;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(length = 50)
    private String category;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum NotificationChannelType {
        EMAIL, SMS, PUSH, IN_APP, WEBHOOK
    }
    
    public boolean isEmailType() {
        return this.type == NotificationChannelType.EMAIL;
    }
    
    public boolean isSmsType() {
        return this.type == NotificationChannelType.SMS;
    }
    
    public static class Templates {
        public static final String ORDER_CREATED = "ORDER_CREATED";
        public static final String ORDER_CONFIRMED = "ORDER_CONFIRMED";
        public static final String ORDER_SHIPPED = "ORDER_SHIPPED";
        public static final String ORDER_DELIVERED = "ORDER_DELIVERED";
        public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
        public static final String ORDER_RETURNED = "ORDER_RETURNED";
        public static final String ORDER_REFUNDED = "ORDER_REFUNDED";
        
        public static final String USER_WELCOME = "USER_WELCOME";
        public static final String USER_EMAIL_VERIFICATION = "USER_EMAIL_VERIFICATION";
        public static final String USER_PASSWORD_RESET = "USER_PASSWORD_RESET";
        public static final String USER_APPROVED = "USER_APPROVED";
        public static final String USER_REJECTED = "USER_REJECTED";
        
        public static final String SYSTEM_MAINTENANCE = "SYSTEM_MAINTENANCE";
        public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
        public static final String SYSTEM_BACKUP = "SYSTEM_BACKUP";
        
        public static final String PAYMENT_SUCCESS = "PAYMENT_SUCCESS";
        public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
        public static final String PAYMENT_REFUND = "PAYMENT_REFUND";
        public static final String INVENTORY_LOW = "INVENTORY_LOW";
        public static final String INVENTORY_OUT = "INVENTORY_OUT";
        public static final String SETTLEMENT_READY = "SETTLEMENT_READY";
        public static final String SETTLEMENT_COMPLETED = "SETTLEMENT_COMPLETED";
    }
    
    public static NotificationTemplate createEmailTemplate(String name, String subject, String content) {
        NotificationTemplate template = new NotificationTemplate();
        template.setName(name);
        template.setType(NotificationChannelType.EMAIL);
        template.setSubject(subject);
        template.setContent(content);
        template.setIsActive(true);
        return template;
    }
    
    public static NotificationTemplate createSmsTemplate(String name, String content) {
        NotificationTemplate template = new NotificationTemplate();
        template.setName(name);
        template.setType(NotificationChannelType.SMS);
        template.setSmsContent(content);
        template.setIsActive(true);
        return template;
    }
}