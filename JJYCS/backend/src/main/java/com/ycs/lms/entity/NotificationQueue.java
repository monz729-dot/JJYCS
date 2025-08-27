package com.ycs.lms.entity;

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
    
    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "channel", nullable = false, length = 20)
    private String channel; // EMAIL, SMS, PUSH, etc.
    
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, SENT, FAILED
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
    
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }
    
    public boolean isSent() {
        return "SENT".equals(this.status);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(this.status);
    }
}