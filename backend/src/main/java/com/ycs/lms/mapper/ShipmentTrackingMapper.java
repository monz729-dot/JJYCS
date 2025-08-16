package com.ycs.lms.mapper;

import com.ycs.lms.entity.ShipmentTracking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 배송 추적 MyBatis 매퍼 인터페이스
 * XML 파일에서 구현됨: ShipmentTrackingMapper.xml
 */
@Mapper
public interface ShipmentTrackingMapper {

    // ShipmentTracking CRUD operations
    void insertShipmentTracking(ShipmentTracking tracking);
    void insertShipmentTrackings(List<ShipmentTracking> trackings);
    ShipmentTracking findById(Long id);
    
    // Search operations
    List<ShipmentTracking> findByOrderId(Long orderId);
    List<ShipmentTracking> findByOrderBoxId(Long orderBoxId);
    List<ShipmentTracking> findByTrackingNumber(String trackingNumber);
    List<ShipmentTracking> findByCarrier(String carrier);
    List<ShipmentTracking> findByStatus(String status);
    List<ShipmentTracking> findByShipmentType(String shipmentType);
    
    // Advanced search
    ShipmentTracking findByTrackingNumberAndCarrier(@Param("trackingNumber") String trackingNumber, 
                                                   @Param("carrier") String carrier);
    List<ShipmentTracking> findByCarrierAndStatus(@Param("carrier") String carrier, @Param("status") String status);
    List<ShipmentTracking> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    List<ShipmentTracking> findByCarrierAndDateRange(@Param("carrier") String carrier, 
                                                    @Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);
    List<ShipmentTracking> findPendingShipments();
    List<ShipmentTracking> findInTransitShipments();
    List<ShipmentTracking> findDelayedShipments(@Param("thresholdDays") int thresholdDays);
    
    // Tracking status updates
    List<ShipmentTracking> findByLastUpdatedBefore(LocalDateTime cutoffTime);
    List<ShipmentTracking> findRequiringStatusUpdate();
    ShipmentTracking findLatestByOrderId(Long orderId);
    ShipmentTracking findLatestByOrderBoxId(Long orderBoxId);
    
    // Statistics and reporting
    long countByCarrier(String carrier);
    long countByStatus(String status);
    long countByCarrierAndStatus(@Param("carrier") String carrier, @Param("status") String status);
    long countInTransitByCarrier(String carrier);
    long countDeliveredToday();
    long countPendingShipments();
    
    // Update operations
    void updateShipmentTracking(ShipmentTracking tracking);
    void updateTrackingStatus(@Param("id") Long id, @Param("status") String status, 
                             @Param("statusDescription") String statusDescription);
    void updateTrackingLocation(@Param("id") Long id, @Param("currentLocation") String currentLocation);
    void updateEstimatedDelivery(@Param("id") Long id, @Param("estimatedDeliveryDate") LocalDateTime estimatedDeliveryDate);
    void updateActualDelivery(@Param("id") Long id, @Param("actualDeliveryDate") LocalDateTime actualDeliveryDate);
    void updateLastTracked(@Param("id") Long id, @Param("lastTrackedAt") LocalDateTime lastTrackedAt);
    
    // Batch operations
    void updateTrackingStatusBatch(@Param("ids") List<Long> ids, @Param("status") String status);
    void updateLastTrackedBatch(@Param("trackingNumbers") List<String> trackingNumbers, 
                               @Param("lastTrackedAt") LocalDateTime lastTrackedAt);
    
    // Delete operations
    void deleteById(Long id);
    void deleteByOrderId(Long orderId);
    void deleteByOrderBoxId(Long orderBoxId);
    void deleteOldTrackingRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
}