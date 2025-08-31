package com.ysc.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_recipients")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BulkRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "bulkRecipients"})
    private User user;

    @Column(nullable = false, length = 100)
    private String recipientName; // 수취인 이름

    @Column(length = 20)
    private String recipientPhone; // 수취인 전화번호

    @Column(length = 500)
    private String recipientAddress; // 수취인 주소

    @Column(length = 10)
    private String recipientPostalCode; // 수취인 우편번호

    @Column(nullable = false, length = 50)
    private String country; // 배송 국가

    @Column(length = 100)
    private String recipientEmail; // 수취인 이메일 (선택사항)

    @Column(length = 100)
    private String company; // 수취인 회사명 (선택사항)

    @Column(length = 1000)
    private String specialInstructions; // 특별 요청사항

    @Column(nullable = false)
    private Boolean isActive = true; // 활성 상태

    @Column(length = 200)
    private String notes; // 관리용 메모

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 생성자
    public BulkRecipient(User user, String recipientName, String recipientPhone, 
                        String recipientAddress, String recipientPostalCode, String country) {
        this.user = user;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.recipientAddress = recipientAddress;
        this.recipientPostalCode = recipientPostalCode;
        this.country = country;
        this.isActive = true;
    }

    // 비즈니스 메서드
    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public boolean isOwnedBy(User user) {
        return this.user != null && this.user.getId().equals(user.getId());
    }
}