package com.ysc.lms.repository;

import com.ysc.lms.entity.PhotoRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * P0-5: 사진 촬영 의무 구간 Repository
 */
@Repository
public interface PhotoRequirementRepository extends JpaRepository<PhotoRequirement, Long> {
    
    /**
     * 주문별 사진 요구사항 조회
     */
    List<PhotoRequirement> findByOrderId(Long orderId);
    
    /**
     * 주문별 특정 단계 사진 요구사항 조회
     */
    List<PhotoRequirement> findByOrderIdAndStage(Long orderId, PhotoRequirement.PhotoStage stage);
    
    /**
     * 박스별 사진 요구사항 조회
     */
    List<PhotoRequirement> findByOrderBoxId(Long boxId);
    
    /**
     * 상태별 사진 요구사항 조회
     */
    List<PhotoRequirement> findByStatus(PhotoRequirement.RequirementStatus status);
    
    /**
     * 단계별 사진 요구사항 조회
     */
    List<PhotoRequirement> findByStage(PhotoRequirement.PhotoStage stage);
    
    /**
     * 미완료 필수 사진 요구사항 조회
     */
    @Query("SELECT pr FROM PhotoRequirement pr WHERE pr.isRequired = true " +
           "AND pr.status IN ('PENDING', 'IN_PROGRESS', 'REJECTED') " +
           "ORDER BY pr.createdAt ASC")
    List<PhotoRequirement> findIncompleteRequiredRequirements();
    
    /**
     * 기한 초과 사진 요구사항 조회
     */
    @Query("SELECT pr FROM PhotoRequirement pr WHERE pr.dueAt < :now " +
           "AND pr.status NOT IN ('COMPLETED', 'WAIVED') " +
           "ORDER BY pr.dueAt ASC")
    List<PhotoRequirement> findOverdueRequirements(@Param("now") LocalDateTime now);
    
    /**
     * 에스컬레이션 대상 조회
     */
    @Query("SELECT pr FROM PhotoRequirement pr WHERE pr.isEscalated = false " +
           "AND pr.dueAt < :escalationTime " +
           "AND pr.status NOT IN ('COMPLETED', 'WAIVED') " +
           "ORDER BY pr.dueAt ASC")
    List<PhotoRequirement> findRequirementsForEscalation(@Param("escalationTime") LocalDateTime escalationTime);
    
    /**
     * 알림 발송 대상 조회 (기한 임박)
     */
    @Query("SELECT pr FROM PhotoRequirement pr WHERE pr.dueAt BETWEEN :now AND :alertTime " +
           "AND pr.status IN ('PENDING', 'IN_PROGRESS') " +
           "AND (pr.lastNotificationSentAt IS NULL OR pr.lastNotificationSentAt < :lastAlertTime) " +
           "ORDER BY pr.dueAt ASC")
    List<PhotoRequirement> findRequirementsForNotification(
            @Param("now") LocalDateTime now,
            @Param("alertTime") LocalDateTime alertTime,
            @Param("lastAlertTime") LocalDateTime lastAlertTime);
    
    /**
     * 사용자별 미완료 사진 요구사항 조회
     */
    @Query("SELECT pr FROM PhotoRequirement pr JOIN pr.order o WHERE o.user.id = :userId " +
           "AND pr.status NOT IN ('COMPLETED', 'WAIVED') " +
           "ORDER BY pr.dueAt ASC NULLS LAST")
    List<PhotoRequirement> findIncompleteRequirementsByUserId(@Param("userId") Long userId);
    
    /**
     * 완료자별 완료된 사진 요구사항 조회
     */
    @Query("SELECT pr FROM PhotoRequirement pr WHERE pr.completedBy = :completedBy " +
           "AND pr.status = 'COMPLETED' " +
           "ORDER BY pr.completedAt DESC")
    List<PhotoRequirement> findCompletedRequirementsByUser(@Param("completedBy") String completedBy);
    
    /**
     * 특정 기간 내 완료된 사진 요구사항 조회
     */
    @Query("SELECT pr FROM PhotoRequirement pr WHERE pr.completedAt BETWEEN :startDate AND :endDate " +
           "AND pr.status = 'COMPLETED' " +
           "ORDER BY pr.completedAt DESC")
    List<PhotoRequirement> findCompletedRequirementsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 단계별 완료율 통계
     */
    @Query("SELECT pr.stage, " +
           "COUNT(pr) as total, " +
           "SUM(CASE WHEN pr.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed " +
           "FROM PhotoRequirement pr " +
           "GROUP BY pr.stage")
    List<Object[]> getCompletionStatsByStage();
    
    /**
     * 일별 사진 요구사항 완료 통계
     */
    @Query("SELECT FUNCTION('DATE', pr.completedAt) as date, COUNT(pr) as count " +
           "FROM PhotoRequirement pr " +
           "WHERE pr.status = 'COMPLETED' " +
           "AND pr.completedAt >= :startDate " +
           "GROUP BY FUNCTION('DATE', pr.completedAt) " +
           "ORDER BY date DESC")
    List<Object[]> getDailyCompletionStats(@Param("startDate") LocalDateTime startDate);
    
    /**
     * 평균 완료 시간 통계 (단계별)
     */
    @Query("SELECT pr.stage, " +
           "AVG(FUNCTION('TIMESTAMPDIFF', HOUR, pr.createdAt, pr.completedAt)) as avgHours " +
           "FROM PhotoRequirement pr " +
           "WHERE pr.status = 'COMPLETED' " +
           "AND pr.completedAt IS NOT NULL " +
           "GROUP BY pr.stage")
    List<Object[]> getAverageCompletionTimeByStage();
    
    /**
     * 거부된 사진 요구사항 조회 (재촬영 필요)
     */
    @Query("SELECT pr FROM PhotoRequirement pr WHERE pr.status = 'REJECTED' " +
           "ORDER BY pr.updatedAt DESC")
    List<PhotoRequirement> findRejectedRequirements();
    
    /**
     * 특정 주문의 모든 단계 완료 여부 확인
     */
    @Query("SELECT COUNT(pr) FROM PhotoRequirement pr WHERE pr.order.id = :orderId " +
           "AND pr.isRequired = true " +
           "AND pr.status NOT IN ('COMPLETED', 'WAIVED')")
    Long countIncompleteRequiredByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 주문별 사진 요구사항 완료율 조회
     */
    @Query("SELECT pr.order.id, " +
           "COUNT(pr) as total, " +
           "SUM(CASE WHEN pr.status IN ('COMPLETED', 'WAIVED') THEN 1 ELSE 0 END) as completed " +
           "FROM PhotoRequirement pr " +
           "WHERE pr.order.id IN :orderIds " +
           "GROUP BY pr.order.id")
    List<Object[]> getCompletionRatesByOrderIds(@Param("orderIds") List<Long> orderIds);
    
    /**
     * 높은 우선순위 사진 요구사항 조회 (기한 임박 + 필수)
     */
    @Query("SELECT pr FROM PhotoRequirement pr WHERE pr.isRequired = true " +
           "AND pr.status IN ('PENDING', 'IN_PROGRESS') " +
           "AND pr.dueAt < :urgentTime " +
           "ORDER BY pr.dueAt ASC")
    List<PhotoRequirement> findUrgentRequirements(@Param("urgentTime") LocalDateTime urgentTime);
    
    /**
     * 품질 검증이 필요한 사진 조회 (완료된 것 중 검증 대기)
     */
    @Query("SELECT pr FROM PhotoRequirement pr WHERE pr.status = 'COMPLETED' " +
           "AND pr.completedPhotoCount > 0")
    List<PhotoRequirement> findRequirementsNeedingValidation();
    
    /**
     * 특정 기간 동안의 에스컬레이션 통계
     */
    @Query("SELECT FUNCTION('DATE', pr.escalationAt) as date, COUNT(pr) as count " +
           "FROM PhotoRequirement pr " +
           "WHERE pr.isEscalated = true " +
           "AND pr.escalationAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE', pr.escalationAt) " +
           "ORDER BY date DESC")
    List<Object[]> getEscalationStatsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 단계별 사진 통계 (photoMetadata 대신 stage 기준)
     */
    @Query("SELECT pr.stage, COUNT(pr) " +
           "FROM PhotoRequirement pr " +
           "WHERE pr.status = 'COMPLETED' " +
           "GROUP BY pr.stage " +
           "ORDER BY COUNT(pr) DESC")
    List<Object[]> getPhotoStageStatistics();
}