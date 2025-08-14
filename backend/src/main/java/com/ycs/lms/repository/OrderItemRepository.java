package com.ycs.lms.repository;

import com.ycs.lms.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    List<OrderItem> findByOrderIdOrderByItemOrder(Long orderId);
    
    List<OrderItem> findByHsCode(String hsCode);
    
    List<OrderItem> findByEmsCode(String emsCode);
    
    List<OrderItem> findByCategory(String category);
    
    List<OrderItem> findByRestricted(boolean restricted);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId AND oi.totalAmount > :threshold")
    List<OrderItem> findByOrderIdAndTotalAmountGreaterThan(@Param("orderId") Long orderId, 
                                                          @Param("threshold") BigDecimal threshold);
    
    @Query("SELECT SUM(oi.totalAmount) FROM OrderItem oi WHERE oi.orderId = :orderId")
    BigDecimal sumTotalAmountByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT SUM(oi.unitWeight * oi.quantity) FROM OrderItem oi WHERE oi.orderId = :orderId")
    BigDecimal sumTotalWeightByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.orderId = :orderId")
    long countByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId AND (oi.hsCode IS NULL OR oi.hsCode = '')")
    List<OrderItem> findByOrderIdWithMissingHsCode(@Param("orderId") Long orderId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId AND (oi.emsCode IS NULL OR oi.emsCode = '')")
    List<OrderItem> findByOrderIdWithMissingEmsCode(@Param("orderId") Long orderId);
}