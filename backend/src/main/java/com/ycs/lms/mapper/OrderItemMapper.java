package com.ycs.lms.mapper;

import com.ycs.lms.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 주문 상품 MyBatis 매퍼 인터페이스
 * XML 파일에서 구현됨: OrderItemMapper.xml
 */
@Mapper
public interface OrderItemMapper {

    // OrderItem CRUD operations
    void insertOrderItem(OrderItem item);
    void insertOrderItems(List<OrderItem> items);
    OrderItem findById(Long id);
    
    // Search operations
    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItem> findByOrderIdOrderByItemOrder(Long orderId);
    List<OrderItem> findByHsCode(String hsCode);
    List<OrderItem> findByEmsCode(String emsCode);
    List<OrderItem> findByCategory(String category);
    List<OrderItem> findByRestricted(boolean restricted);
    
    List<OrderItem> findByOrderIdAndTotalAmountGreaterThan(@Param("orderId") Long orderId, 
                                                          @Param("threshold") BigDecimal threshold);
    List<OrderItem> findByOrderIdWithMissingHsCode(Long orderId);
    List<OrderItem> findByOrderIdWithMissingEmsCode(Long orderId);
    
    // Aggregate operations
    BigDecimal sumTotalAmountByOrderId(Long orderId);
    BigDecimal sumTotalWeightByOrderId(Long orderId);
    long countByOrderId(Long orderId);
    
    // Update operations
    void updateOrderItem(OrderItem item);
    
    // Delete operations
    void deleteById(Long id);
    void deleteByOrderId(Long orderId);
    
    // JPA Repository 호환 메서드
    default OrderItem save(OrderItem item) {
        if (item.getId() == null) {
            insertOrderItem(item);
        } else {
            updateOrderItem(item);
        }
        return item;
    }
    
    default List<OrderItem> saveAll(List<OrderItem> items) {
        if (items.size() > 1) {
            insertOrderItems(items);
        } else if (items.size() == 1) {
            save(items.get(0));
        }
        return items;
    }
    
    default Optional<OrderItem> findByIdOptional(Long id) {
        return Optional.ofNullable(findById(id));
    }
    
    default List<OrderItem> findAll() {
        throw new UnsupportedOperationException("Use specific find methods instead");
    }
}