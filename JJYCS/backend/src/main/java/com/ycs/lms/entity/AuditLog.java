package com.ycs.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작업 수행자 정보
    @Column(nullable = false, length = 100)
    private String performedBy; // 사용자 이메일 또는 시스템

    @Column(length = 50)
    private String performerIp; // IP 주소

    @Column(length = 500)
    private String userAgent; // 사용자 에이전트

    // 작업 정보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    @Column(nullable = false, length = 100)
    private String entityType; // 대상 엔티티 타입 (User, Order, etc.)

    @Column
    private Long entityId; // 대상 엔티티 ID

    @Column(length = 200)
    private String description; // 작업 설명

    // 변경 데이터
    @Column(columnDefinition = "TEXT")
    private String oldValues; // 변경 전 데이터 (JSON)

    @Column(columnDefinition = "TEXT")
    private String newValues; // 변경 후 데이터 (JSON)

    // 메타데이터
    @Column(length = 100)
    private String sessionId; // 세션 ID

    @Column(length = 50)
    private String requestId; // 요청 ID (추적용)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultStatus resultStatus = ResultStatus.SUCCESS;

    @Column(length = 1000)
    private String errorMessage; // 오류 발생 시 메시지

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ActionType {
        // 사용자 관련
        USER_LOGIN,
        USER_LOGOUT,
        USER_REGISTER,
        USER_APPROVE,
        USER_REJECT,
        USER_STATUS_CHANGE,
        USER_UPDATE,
        USER_DELETE,

        // 주문 관련
        ORDER_CREATE,
        ORDER_UPDATE,
        ORDER_STATUS_CHANGE,
        ORDER_DELETE,

        // 결제 관련
        PAYMENT_CREATE,
        PAYMENT_UPDATE,
        PAYMENT_CONFIRM,

        // 알림 관련
        NOTIFICATION_SEND,
        NOTIFICATION_READ,

        // 시스템 관리
        SYSTEM_CONFIG_CHANGE,
        DATA_EXPORT,
        DATA_IMPORT,
        BULK_OPERATION,

        // 기타
        FILE_UPLOAD,
        FILE_DOWNLOAD,
        FILE_DELETE,
        API_ACCESS,
        SECURITY_EVENT
    }

    public enum ResultStatus {
        SUCCESS,    // 성공
        FAILURE,    // 실패
        ERROR       // 오류
    }

    // 생성자
    public AuditLog(String performedBy, ActionType actionType, String entityType, Long entityId, String description) {
        this.performedBy = performedBy;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
    }

    public AuditLog(String performedBy, ActionType actionType, String entityType, Long entityId, 
                   String description, String oldValues, String newValues) {
        this(performedBy, actionType, entityType, entityId, description);
        this.oldValues = oldValues;
        this.newValues = newValues;
    }

    // Helper methods
    public void setError(String errorMessage) {
        this.resultStatus = ResultStatus.ERROR;
        this.errorMessage = errorMessage;
    }

    public void setFailure(String errorMessage) {
        this.resultStatus = ResultStatus.FAILURE;
        this.errorMessage = errorMessage;
    }
}