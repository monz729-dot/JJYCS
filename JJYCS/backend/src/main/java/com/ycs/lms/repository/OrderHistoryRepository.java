package com.ycs.lms.repository;

import com.ycs.lms.entity.OrderHistory;
import com.ycs.lms.entity.Order;
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
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    
    @EntityGraph(attributePaths = {"order"})
    List<OrderHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    @EntityGraph(attributePaths = {"order"})
    Page<OrderHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"order"})
    List<OrderHistory> findByOrderOrderByCreatedAtDesc(Order order);
    
    @Query(value = "SELECT oh FROM OrderHistory oh JOIN FETCH oh.order WHERE oh.order.user.id = :userId ORDER BY oh.createdAt DESC",
           countQuery = "SELECT COUNT(oh) FROM OrderHistory oh WHERE oh.order.user.id = :userId")
    Page<OrderHistory> findByOrderUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT oh FROM OrderHistory oh JOIN FETCH oh.order WHERE oh.createdAt BETWEEN :startDate AND :endDate ORDER BY oh.createdAt DESC")
    List<OrderHistory> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("startDate") LocalDateTime startDate, 
                                                                 @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT oh FROM OrderHistory oh JOIN FETCH oh.order WHERE oh.newStatus = :status ORDER BY oh.createdAt DESC")
    List<OrderHistory> findByNewStatusOrderByCreatedAtDesc(@Param("status") Order.OrderStatus status);
    
    @Query("SELECT COUNT(oh) FROM OrderHistory oh WHERE oh.order.id = :orderId")
    Long countByOrderId(@Param("orderId") Long orderId);
    
    // 최근 24시간 내 변경 이력
    @Query("SELECT oh FROM OrderHistory oh JOIN FETCH oh.order WHERE oh.createdAt >= :since ORDER BY oh.createdAt DESC")
    List<OrderHistory> findRecentChanges(@Param("since") LocalDateTime since);
}