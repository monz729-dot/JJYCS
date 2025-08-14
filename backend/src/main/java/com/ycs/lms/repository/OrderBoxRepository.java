package com.ycs.lms.repository;

import com.ycs.lms.entity.OrderBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderBoxRepository extends JpaRepository<OrderBox, Long> {
    
    List<OrderBox> findByOrderId(Long orderId);
    
    List<OrderBox> findByOrderIdOrderByBoxNumber(Long orderId);
    
    Optional<OrderBox> findByLabelCode(String labelCode);
    
    List<OrderBox> findByLabelCodeIn(List<String> labelCodes);
    
    boolean existsByLabelCode(String labelCode);
    
    List<OrderBox> findByStatus(OrderBox.BoxStatus status);
    
    List<OrderBox> findByOrderIdAndStatus(Long orderId, OrderBox.BoxStatus status);
    
    List<OrderBox> findByWarehouseId(Long warehouseId);
    
    List<OrderBox> findByWarehouseIdAndStatus(Long warehouseId, OrderBox.BoxStatus status);
    
    List<OrderBox> findByWarehouseLocation(String warehouseLocation);
    
    List<OrderBox> findByTrackingNumber(String trackingNumber);
    
    List<OrderBox> findByCarrier(String carrier);
    
    @Query("SELECT ob FROM OrderBox ob WHERE ob.orderId = :orderId AND ob.cbmM3 > :cbmThreshold")
    List<OrderBox> findByOrderIdAndCbmGreaterThan(@Param("orderId") Long orderId, 
                                                 @Param("cbmThreshold") BigDecimal cbmThreshold);
    
    @Query("SELECT SUM(ob.cbmM3) FROM OrderBox ob WHERE ob.orderId = :orderId")
    BigDecimal sumCbmByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT SUM(ob.weightKg) FROM OrderBox ob WHERE ob.orderId = :orderId")
    BigDecimal sumWeightByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT COUNT(ob) FROM OrderBox ob WHERE ob.orderId = :orderId")
    long countByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT ob FROM OrderBox ob WHERE ob.labelCode LIKE :pattern")
    List<OrderBox> findByLabelCodePattern(@Param("pattern") String pattern);
    
    @Query("SELECT ob FROM OrderBox ob WHERE ob.status IN :statuses")
    List<OrderBox> findByStatusIn(@Param("statuses") List<OrderBox.BoxStatus> statuses);
    
    @Query("SELECT COUNT(ob) FROM OrderBox ob WHERE ob.status = :status")
    long countByStatus(@Param("status") OrderBox.BoxStatus status);
    
    @Query("SELECT COUNT(ob) FROM OrderBox ob WHERE ob.warehouseId = :warehouseId AND ob.status = :status")
    long countByWarehouseIdAndStatus(@Param("warehouseId") Long warehouseId, 
                                    @Param("status") OrderBox.BoxStatus status);
}