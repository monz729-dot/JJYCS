package com.ycs.lms.mapper;

import com.ycs.lms.entity.OrderBox;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 주문 박스 MyBatis 매퍼 인터페이스
 * XML 파일에서 구현됨: OrderBoxMapper.xml
 */
@Mapper
public interface OrderBoxMapper {

    // OrderBox CRUD operations
    void insertOrderBox(OrderBox box);
    void insertOrderBoxes(List<OrderBox> boxes);
    OrderBox findById(Long id);
    
    // Search operations
    List<OrderBox> findByOrderId(Long orderId);
    List<OrderBox> findByOrderIdOrderByBoxNumber(Long orderId);
    OrderBox findByLabelCode(String labelCode);
    List<OrderBox> findByLabelCodeIn(List<String> labelCodes);
    boolean existsByLabelCode(String labelCode);
    
    List<OrderBox> findByStatus(String status);
    List<OrderBox> findByOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") String status);
    List<OrderBox> findByWarehouseId(Long warehouseId);
    List<OrderBox> findByWarehouseIdAndStatus(@Param("warehouseId") Long warehouseId, @Param("status") String status);
    List<OrderBox> findByWarehouseLocation(String warehouseLocation);
    List<OrderBox> findByTrackingNumber(String trackingNumber);
    List<OrderBox> findByCarrier(String carrier);
    
    List<OrderBox> findByOrderIdAndCbmGreaterThan(@Param("orderId") Long orderId, 
                                                 @Param("cbmThreshold") BigDecimal cbmThreshold);
    List<OrderBox> findByLabelCodePattern(String pattern);
    List<OrderBox> findByStatusIn(List<String> statuses);
    
    // Aggregate operations
    BigDecimal sumCbmByOrderId(Long orderId);
    BigDecimal sumWeightByOrderId(Long orderId);
    long countByOrderId(Long orderId);
    long countByStatus(String status);
    long countByWarehouseIdAndStatus(@Param("warehouseId") Long warehouseId, @Param("status") String status);
    
    // Label code generation
    String generateLabelCode(@Param("orderSeq") int orderSeq, @Param("boxNumber") int boxNumber);
    
    // Update operations
    void updateOrderBox(OrderBox box);
    void updateOrderBoxStatus(OrderBox box);
    void updateBoxInbound(@Param("id") Long id, @Param("warehouseId") Long warehouseId, 
                         @Param("warehouseLocation") String warehouseLocation);
    void updateBoxOutbound(@Param("id") Long id, @Param("trackingNumber") String trackingNumber, 
                          @Param("carrier") String carrier);
    
    // Delete operations
    void deleteById(Long id);
    void deleteByOrderId(Long orderId);
    
    // JPA Repository 호환 메서드
    default OrderBox save(OrderBox box) {
        if (box.getId() == null) {
            insertOrderBox(box);
        } else {
            updateOrderBox(box);
        }
        return box;
    }
    
    default List<OrderBox> saveAll(List<OrderBox> boxes) {
        for (OrderBox box : boxes) {
            save(box);
        }
        return boxes;
    }
    
    default Optional<OrderBox> findByLabelCodeOptional(String labelCode) {
        return Optional.ofNullable(findByLabelCode(labelCode));
    }
    
    default List<OrderBox> findAll() {
        throw new UnsupportedOperationException("Use specific find methods instead");
    }
}