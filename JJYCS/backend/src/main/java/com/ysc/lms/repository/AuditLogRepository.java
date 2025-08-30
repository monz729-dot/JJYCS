package com.ysc.lms.repository;

import com.ysc.lms.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // 사용자별 감사 로그
    Page<AuditLog> findByPerformedByOrderByCreatedAtDesc(String performedBy, Pageable pageable);
    
    // 액션 타입별 감사 로그
    Page<AuditLog> findByActionTypeOrderByCreatedAtDesc(AuditLog.ActionType actionType, Pageable pageable);
    
    // 엔티티별 감사 로그
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);
    
    // 기간별 감사 로그
    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // 결과 상태별 감사 로그
    Page<AuditLog> findByResultStatusOrderByCreatedAtDesc(AuditLog.ResultStatus resultStatus, Pageable pageable);
    
    // 복합 검색
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:performedBy IS NULL OR a.performedBy LIKE %:performedBy%) AND " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByFilters(@Param("performedBy") String performedBy,
                                @Param("actionType") AuditLog.ActionType actionType,
                                @Param("entityType") String entityType,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                Pageable pageable);
    
    // 통계용 쿼리들
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt >= :since")
    Long countLogsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT a.actionType, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :since GROUP BY a.actionType")
    List<Object[]> getActionTypeStatsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT a.resultStatus, COUNT(a) FROM AuditLog a WHERE a.createdAt >= :since GROUP BY a.resultStatus")
    List<Object[]> getResultStatusStatsSince(@Param("since") LocalDateTime since);
    
    // 보안 이벤트
    @Query("SELECT a FROM AuditLog a WHERE a.actionType = 'SECURITY_EVENT' OR a.resultStatus != 'SUCCESS' ORDER BY a.createdAt DESC")
    Page<AuditLog> findSecurityEvents(Pageable pageable);
    
    // 최근 활동
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLog> findRecentActivity(@Param("since") LocalDateTime since);
}