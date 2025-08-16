package com.ycs.lms.mapper;

import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.OrderBox;
import com.ycs.lms.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 주문 MyBatis 매퍼 인터페이스
 * XML 파일에서 구현됨: OrderMapper.xml
 */
@Mapper
public interface OrderMapper {

    // Order CRUD operations
    void insertOrder(Order order);
    Order findOrderById(Long orderId);
    Order findOrderByIdWithDetails(Long orderId);
    
    // Order search and filtering
    List<Order> findOrders(@Param("userId") Long userId, @Param("status") String status, 
                          @Param("orderType") String orderType, @Param("startDate") String startDate, 
                          @Param("endDate") String endDate, @Param("limit") int limit, @Param("offset") int offset);
    long countOrders(@Param("userId") Long userId, @Param("status") String status, 
                    @Param("orderType") String orderType, @Param("startDate") String startDate, 
                    @Param("endDate") String endDate);
    
    List<Order> findByUserId(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status, 
                                     @Param("limit") int limit, @Param("offset") int offset);
    List<Order> findByStatus(@Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);
    
    long countByUserId(Long userId);
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);
    BigDecimal sumTotalAmountByUserIdAndCompleted(@Param("userId") Long userId);
    
    // Update operations
    void updateOrder(Order order);
    void cancelOrder(Long orderId);
    
    // Delete operations
    void deleteById(Long id);

    // Business logic queries
    BigDecimal getTotalCbmByOrderId(Long orderId);
    BigDecimal getTotalAmountByOrderId(Long orderId);
    boolean hasMemberCodeIssue(Long userId);
    String generateOrderCode();
    int getNextOrderSequence();
    
    // JPA Repository 호환 메서드
    default Optional<Order> findById(Long id) {
        Order order = findOrderById(id);
        return Optional.ofNullable(order);
    }
    
    default Optional<Order> findByIdWithDetails(Long id) {
        Order order = findOrderByIdWithDetails(id);
        return Optional.ofNullable(order);
    }
    
    default Order save(Order order) {
        if (order.getId() == null) {
            insertOrder(order);
        } else {
            updateOrder(order);
        }
        return order;
    }
    
    // Pageable 대체 메서드
    default List<Order> findByFilter(Long userId, String status, String orderType, int page, int size) {
        int offset = page * size;
        return findOrders(userId, status, orderType, null, null, size, offset);
    }
}