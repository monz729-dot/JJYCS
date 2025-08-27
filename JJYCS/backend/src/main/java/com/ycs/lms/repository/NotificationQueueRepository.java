package com.ycs.lms.repository;

import com.ycs.lms.entity.NotificationQueue;
import com.ycs.lms.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationQueueRepository extends JpaRepository<NotificationQueue, Long> {
    
    List<NotificationQueue> findByStatusOrderByScheduledAtAsc(NotificationQueue.SendStatus status);
    
    List<NotificationQueue> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    
    List<NotificationQueue> findByRelatedOrderIdOrderByCreatedAtDesc(Long orderId);
    
    List<NotificationQueue> findByTypeAndStatusOrderByScheduledAtAsc(
        NotificationTemplate.NotificationType type, 
        NotificationQueue.SendStatus status
    );
    
    @Query("SELECT nq FROM NotificationQueue nq WHERE nq.status IN ('PENDING', 'RETRY') " +
           "AND (nq.scheduledAt IS NULL OR nq.scheduledAt <= :now) " +
           "ORDER BY nq.scheduledAt ASC, nq.createdAt ASC")
    List<NotificationQueue> findReadyToSend(@Param("now") LocalDateTime now);
    
    @Query("SELECT nq FROM NotificationQueue nq WHERE nq.status = 'FAILED' " +
           "AND nq.retryCount < nq.maxRetry " +
           "AND nq.updatedAt <= :retryAfter " +
           "ORDER BY nq.updatedAt ASC")
    List<NotificationQueue> findRetryableNotifications(@Param("retryAfter") LocalDateTime retryAfter);
    
    @Query("SELECT COUNT(nq) FROM NotificationQueue nq WHERE nq.status = :status " +
           "AND nq.createdAt >= :since")
    Long countByStatusSince(@Param("status") NotificationQueue.SendStatus status,
                           @Param("since") LocalDateTime since);
    
    @Query("SELECT nq.status, COUNT(nq) FROM NotificationQueue nq " +
           "WHERE nq.createdAt >= :since " +
           "GROUP BY nq.status")
    List<Object[]> getStatusStatsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT nq.type, COUNT(nq) FROM NotificationQueue nq " +
           "WHERE nq.createdAt >= :since " +
           "GROUP BY nq.type")
    List<Object[]> getTypeStatsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT DATE(nq.createdAt), COUNT(nq) FROM NotificationQueue nq " +
           "WHERE nq.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(nq.createdAt) " +
           "ORDER BY DATE(nq.createdAt)")
    List<Object[]> getDailyStatistics(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(TIMESTAMPDIFF(SECOND, nq.createdAt, nq.sentAt)) FROM NotificationQueue nq " +
           "WHERE nq.status = 'SENT' AND nq.sentAt IS NOT NULL " +
           "AND nq.createdAt >= :since")
    Double getAverageDeliveryTimeSeconds(@Param("since") LocalDateTime since);
    
    @Query("SELECT nq FROM NotificationQueue nq WHERE nq.status = 'PROCESSING' " +
           "AND nq.updatedAt <= :stuckThreshold")
    List<NotificationQueue> findStuckProcessingNotifications(@Param("stuckThreshold") LocalDateTime stuckThreshold);
    
    List<NotificationQueue> findByExternalId(String externalId);
    
    @Query("DELETE FROM NotificationQueue nq WHERE nq.status IN ('SENT', 'DELIVERED', 'CANCELLED') " +
           "AND nq.sentAt <= :beforeDate")
    void deleteOldNotifications(@Param("beforeDate") LocalDateTime beforeDate);
}