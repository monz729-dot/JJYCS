package com.ycs.lms.repository;

import com.ycs.lms.entity.Notification;
import com.ycs.lms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @EntityGraph(attributePaths = {"user", "relatedOrder"})
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    @EntityGraph(attributePaths = {"user", "relatedOrder"})
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    @EntityGraph(attributePaths = {"user", "relatedOrder"})
    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead);
    
    @EntityGraph(attributePaths = {"user", "relatedOrder"})
    Page<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead, Pageable pageable);
    
    @Query(value = "SELECT n FROM Notification n JOIN FETCH n.user WHERE n.user.id = :userId ORDER BY n.createdAt DESC",
           countQuery = "SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId")
    Page<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT n FROM Notification n JOIN FETCH n.user WHERE n.user.id = :userId AND n.isRead = :isRead ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("isRead") Boolean isRead);
    
    @Query(value = "SELECT n FROM Notification n JOIN FETCH n.user WHERE n.user.id = :userId AND n.isRead = :isRead ORDER BY n.createdAt DESC",
           countQuery = "SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = :isRead")
    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("isRead") Boolean isRead, Pageable pageable);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.type = :type AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findByTypeAndCreatedAtAfter(@Param("type") Notification.NotificationType type, 
                                                  @Param("since") LocalDateTime since);
    
    @Query("SELECT n FROM Notification n WHERE n.isEmailSent = false AND n.type IN :types ORDER BY n.createdAt ASC")
    List<Notification> findPendingEmailNotifications(@Param("types") List<Notification.NotificationType> types);
    
    @Query("SELECT n FROM Notification n WHERE n.relatedOrder.id = :orderId ORDER BY n.createdAt DESC")
    List<Notification> findByRelatedOrderId(@Param("orderId") Long orderId);
    
    // 최근 24시간 내 알림
    @Query("SELECT n FROM Notification n JOIN FETCH n.user WHERE n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("since") LocalDateTime since);
    
    // 고급 알림 검색
    @Query(value = "SELECT n FROM Notification n JOIN FETCH n.user u WHERE " +
           "(:userId IS NULL OR u.id = :userId) AND " +
           "(:type IS NULL OR n.type = :type) AND " +
           "(:isRead IS NULL OR n.isRead = :isRead) AND " +
           "(:startDate IS NULL OR n.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR n.createdAt <= :endDate) AND " +
           "(:keyword IS NULL OR n.title LIKE %:keyword% OR n.message LIKE %:keyword%) " +
           "ORDER BY n.createdAt DESC",
           countQuery = "SELECT COUNT(n) FROM Notification n JOIN n.user u WHERE " +
           "(:userId IS NULL OR u.id = :userId) AND " +
           "(:type IS NULL OR n.type = :type) AND " +
           "(:isRead IS NULL OR n.isRead = :isRead) AND " +
           "(:startDate IS NULL OR n.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR n.createdAt <= :endDate) AND " +
           "(:keyword IS NULL OR n.title LIKE %:keyword% OR n.message LIKE %:keyword%)")
    Page<Notification> searchNotifications(@Param("userId") Long userId,
                                          @Param("type") Notification.NotificationType type,
                                          @Param("isRead") Boolean isRead,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);

    // 타입별 알림 통계
    @Query("SELECT n.type, COUNT(n) FROM Notification n WHERE " +
           "(:userId IS NULL OR n.user.id = :userId) AND " +
           "(:startDate IS NULL OR n.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR n.createdAt <= :endDate) " +
           "GROUP BY n.type")
    List<Object[]> getNotificationTypeStats(@Param("userId") Long userId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // 읽음 상태별 통계
    @Query("SELECT n.isRead, COUNT(n) FROM Notification n WHERE " +
           "(:userId IS NULL OR n.user.id = :userId) AND " +
           "(:startDate IS NULL OR n.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR n.createdAt <= :endDate) " +
           "GROUP BY n.isRead")
    List<Object[]> getNotificationReadStats(@Param("userId") Long userId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // 우선순위가 높은 알림 (읽지 않은 시스템/보안 관련)
    @Query(value = "SELECT n FROM Notification n JOIN FETCH n.user WHERE " +
           "n.isRead = false AND " +
           "n.type IN ('SYSTEM_MAINTENANCE', 'USER_STATUS_CHANGED', 'CBM_THRESHOLD_EXCEEDED', 'THB_THRESHOLD_EXCEEDED', 'NO_MEMBER_CODE_WARNING') " +
           "ORDER BY n.createdAt DESC",
           countQuery = "SELECT COUNT(n) FROM Notification n WHERE " +
           "n.isRead = false AND " +
           "n.type IN ('SYSTEM_MAINTENANCE', 'USER_STATUS_CHANGED', 'CBM_THRESHOLD_EXCEEDED', 'THB_THRESHOLD_EXCEEDED', 'NO_MEMBER_CODE_WARNING')")
    Page<Notification> findHighPriorityNotifications(Pageable pageable);
}