package com.ysc.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_queue")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationQueue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User recipient;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationTemplate.NotificationType type;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "channel", nullable = false, length = 20)
    private String channel; // EMAIL, SMS, PUSH, etc.
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SendStatus status = SendStatus.PENDING;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "max_retry")
    private Integer maxRetry = 3;
    
    @Column(name = "external_id", length = 100)
    private String externalId;
    
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public boolean canRetry() {
        return retryCount < maxRetry;
    }
    
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    public boolean isPending() {
        return SendStatus.PENDING.equals(this.status);
    }
    
    public boolean isSent() {
        return SendStatus.SENT.equals(this.status);
    }
    
    public boolean isFailed() {
        return SendStatus.FAILED.equals(this.status);
    }
    
    public enum SendStatus {
        PENDING, SENT, FAILED
    }
}