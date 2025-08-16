package com.ycs.lms.repository;

import com.ycs.lms.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 주문 리포지토리
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 주문 ID로 상세 정보 조회 (연관 엔티티 포함)
     */
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.items " +
           "LEFT JOIN FETCH o.boxes " +
           "WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Long orderId);

    /**
     * 필터 조건으로 주문 목록 조회
     */
    @Query("SELECT o FROM Order o " +
           "WHERE (:userId IS NULL OR o.user.id = :userId) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:orderType IS NULL OR o.orderType = :orderType) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findByFilter(@Param("userId") Long userId,
                            @Param("status") String status,
                            @Param("orderType") String orderType,
                            Pageable pageable);

    /**
     * 사용자별 주문 조회
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 사용자별 상태별 주문 조회
     */
    Page<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status, Pageable pageable);
    
    /**
     * 상태별 주문 조회
     */
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    /**
     * 사용자별 주문 수 조회
     */
    long countByUserId(Long userId);
    
    /**
     * 사용자별 상태별 주문 수 조회
     */
    long countByUserIdAndStatus(Long userId, Order.OrderStatus status);

    /**
     * 다음 주문 시퀀스 번호 조회
     */
    @Query("SELECT COUNT(o) + 1 FROM Order o WHERE YEAR(o.createdAt) = YEAR(CURRENT_DATE)")
    int getNextOrderSequence();
    
    /**
     * 사용자별 완료 주문 총액 조회
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.userId = :userId AND (o.status = 'DELIVERED' OR o.status = 'COMPLETED')")
    BigDecimal sumTotalAmountByUserIdAndCompleted(@Param("userId") Long userId);
    
    /**
     * 사용자별 최근 주문 조회
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
}