package com.ysc.lms.repository;

import com.ysc.lms.entity.OrderTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderTrackingRepository extends JpaRepository<OrderTracking, Long> {
    
    List<OrderTracking> findByOrderIdOrderByEventTimeDesc(Long orderId);
    
    List<OrderTracking> findByOrderOrderNumberOrderByEventTimeDesc(String orderNumber);
    
    List<OrderTracking> findByTrackingNumber(String trackingNumber);
    
    Optional<OrderTracking> findFirstByOrderIdOrderByEventTimeDesc(Long orderId);
    
    List<OrderTracking> findByStatusOrderByEventTimeDesc(OrderTracking.TrackingStatus status);
    
    List<OrderTracking> findByIsMilestoneAndOrderIdOrderByEventTimeAsc(Boolean isMilestone, Long orderId);
    
    @Query("SELECT ot FROM OrderTracking ot WHERE ot.order.id = :orderId " +
           "AND ot.eventTime BETWEEN :startDate AND :endDate " +
           "ORDER BY ot.eventTime DESC")
    List<OrderTracking> findByOrderAndDateRange(
        @Param("orderId") Long orderId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT ot FROM OrderTracking ot WHERE ot.status IN :statuses " +
           "ORDER BY ot.eventTime DESC")
    List<OrderTracking> findByStatusIn(@Param("statuses") List<OrderTracking.TrackingStatus> statuses);
    
    @Query("SELECT COUNT(ot) FROM OrderTracking ot WHERE ot.status = :status " +
           "AND ot.eventTime >= :sinceDate")
    Long countByStatusSince(@Param("status") OrderTracking.TrackingStatus status, 
                           @Param("sinceDate") LocalDateTime sinceDate);
    
    @Query("SELECT ot FROM OrderTracking ot WHERE ot.estimatedDelivery IS NOT NULL " +
           "AND ot.estimatedDelivery BETWEEN :startDate AND :endDate " +
           "AND ot.status NOT IN ('DELIVERED', 'CANCELLED', 'RETURNED_TO_SENDER') " +
           "ORDER BY ot.estimatedDelivery ASC")
    List<OrderTracking> findPendingDeliveriesByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT ot FROM OrderTracking ot WHERE ot.order.user.id = :userId " +
           "ORDER BY ot.eventTime DESC")
    List<OrderTracking> findByUserIdOrderByEventTimeDesc(@Param("userId") Long userId);
    
    @Query("SELECT ot.locationCode, COUNT(ot) FROM OrderTracking ot " +
           "WHERE ot.eventTime BETWEEN :startDate AND :endDate " +
           "AND ot.locationCode IS NOT NULL " +
           "GROUP BY ot.locationCode " +
           "ORDER BY COUNT(ot) DESC")
    List<Object[]> getLocationStatsByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT ot FROM OrderTracking ot WHERE ot.status IN " +
           "('DELAYED', 'HELD_AT_CUSTOMS', 'DELIVERY_FAILED') " +
           "AND ot.eventTime >= :sinceDate " +
           "ORDER BY ot.eventTime DESC")
    List<OrderTracking> findRecentExceptions(@Param("sinceDate") LocalDateTime sinceDate);
    
    @Query("SELECT DISTINCT ot.order.id FROM OrderTracking ot " +
           "WHERE ot.status = :status " +
           "AND ot.eventTime >= :sinceDate")
    List<Long> findOrderIdsByStatusSince(@Param("status") OrderTracking.TrackingStatus status,
                                        @Param("sinceDate") LocalDateTime sinceDate);
}