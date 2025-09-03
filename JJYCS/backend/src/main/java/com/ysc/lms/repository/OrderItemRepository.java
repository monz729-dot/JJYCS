package com.ysc.lms.repository;

import com.ysc.lms.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // 주문별 아이템 조회
    List<OrderItem> findByOrderIdOrderByIdAsc(Long orderId);
    
    // 주문별 아이템 개수
    long countByOrderId(Long orderId);
    
    // 주문별 총 수량 계산
    @Query("SELECT COALESCE(SUM(oi.qty), 0) FROM OrderItem oi WHERE oi.orderId = :orderId")
    Integer sumQtyByOrderId(@Param("orderId") Long orderId);
    
    // 주문별 총 금액 계산 (서버 검증용)
    @Query("SELECT COALESCE(SUM(oi.amount), 0) FROM OrderItem oi WHERE oi.orderId = :orderId")
    java.math.BigDecimal sumAmountByOrderId(@Param("orderId") Long orderId);
    
    // 주문 삭제 시 연관된 아이템들 삭제
    void deleteByOrderId(Long orderId);
    
    // 특정 상품명으로 검색
    List<OrderItem> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);
    
    // 최근 주문한 상품들 (사용자별)
    @Query("SELECT oi FROM OrderItem oi JOIN Order o ON oi.orderId = o.id " +
           "WHERE o.userId = :userId ORDER BY oi.createdAt DESC")
    List<OrderItem> findRecentItemsByUserId(@Param("userId") Long userId);
}