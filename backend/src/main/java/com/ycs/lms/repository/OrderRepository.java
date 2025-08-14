package com.ycs.lms.repository;

import com.ycs.lms.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderCode(String orderCode);
    
    boolean existsByOrderCode(String orderCode);
    
    List<Order> findByUserId(Long userId);
    
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);
    
    Page<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status, Pageable pageable);
    
    List<Order> findByOrderType(Order.OrderType orderType);
    
    List<Order> findByUserIdAndOrderType(Long userId, Order.OrderType orderType);
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                        @Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.totalCbmM3 > :cbmThreshold")
    List<Order> findByTotalCbmGreaterThan(@Param("cbmThreshold") BigDecimal cbmThreshold);
    
    @Query("SELECT o FROM Order o WHERE o.totalAmount > :amountThreshold")
    List<Order> findByTotalAmountGreaterThan(@Param("amountThreshold") BigDecimal amountThreshold);
    
    @Query("SELECT o FROM Order o WHERE o.requiresExtraRecipient = true")
    List<Order> findOrdersRequiringExtraRecipient();
    
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    List<Order> findByStatusIn(@Param("statuses") List<Order.OrderStatus> statuses);
    
    @Query("SELECT o FROM Order o WHERE o.estimatedDeliveryDate = :date")
    List<Order> findByEstimatedDeliveryDate(@Param("date") LocalDate date);
    
    @Query("SELECT o FROM Order o WHERE o.estimatedDeliveryDate BETWEEN :startDate AND :endDate")
    List<Order> findByEstimatedDeliveryDateRange(@Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);
    
    // Statistics queries
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") Order.OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Order.OrderStatus status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.userId = :userId AND o.paymentStatus = 'COMPLETED'")
    BigDecimal sumTotalAmountByUserIdAndCompleted(@Param("userId") Long userId);
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
}