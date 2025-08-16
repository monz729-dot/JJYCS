package com.ycs.lms.mapper;

import com.ycs.lms.entity.ScanEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 스캔 이벤트 MyBatis 매퍼 인터페이스
 * XML 파일에서 구현됨: ScanEventMapper.xml
 */
@Mapper
public interface ScanEventMapper {

    // ScanEvent CRUD operations
    void insertScanEvent(ScanEvent scanEvent);
    void insertScanEvents(List<ScanEvent> scanEvents);
    ScanEvent findById(Long id);
    
    // Search operations
    List<ScanEvent> findByLabelCode(String labelCode);
    List<ScanEvent> findByOrderId(Long orderId);
    List<ScanEvent> findByOrderBoxId(Long orderBoxId);
    List<ScanEvent> findByWarehouseId(Long warehouseId);
    List<ScanEvent> findByUserId(Long userId);
    List<ScanEvent> findByScanType(String scanType);
    List<ScanEvent> findByStatus(String status);
    
    // Advanced search
    List<ScanEvent> findByWarehouseIdAndScanType(@Param("warehouseId") Long warehouseId, @Param("scanType") String scanType);
    List<ScanEvent> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    List<ScanEvent> findByWarehouseIdAndDateRange(@Param("warehouseId") Long warehouseId, 
                                                 @Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);
    List<ScanEvent> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                            @Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    List<ScanEvent> findRecentByWarehouseId(@Param("warehouseId") Long warehouseId, @Param("limit") int limit);
    List<ScanEvent> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    
    // Scan history and tracking
    List<ScanEvent> findScanHistoryByLabelCode(String labelCode);
    List<ScanEvent> findScanHistoryByOrderId(Long orderId);
    ScanEvent findLastScanByLabelCode(String labelCode);
    ScanEvent findLastScanByOrderBoxId(Long orderBoxId);
    List<ScanEvent> findPendingScansByWarehouseId(Long warehouseId);
    
    // Statistics and reporting
    long countByWarehouseId(Long warehouseId);
    long countByWarehouseIdAndScanType(@Param("warehouseId") Long warehouseId, @Param("scanType") String scanType);
    long countByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDateTime date);
    long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Update operations
    void updateScanEvent(ScanEvent scanEvent);
    void updateScanEventStatus(@Param("id") Long id, @Param("status") String status);
    void updateScanEventNotes(@Param("id") Long id, @Param("notes") String notes);
    
    // Filtered search with pagination
    List<ScanEvent> findScanEventsWithFilter(@Param("warehouseId") Long warehouseId,
                                            @Param("boxId") Long boxId,
                                            @Param("eventType") String eventType,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate,
                                            @Param("userId") Long userId,
                                            @Param("labelCode") String labelCode,
                                            @Param("orderCode") String orderCode,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset);
    
    long countScanEventsWithFilter(@Param("warehouseId") Long warehouseId,
                                  @Param("boxId") Long boxId,
                                  @Param("eventType") String eventType,
                                  @Param("startDate") String startDate,
                                  @Param("endDate") String endDate,
                                  @Param("userId") Long userId,
                                  @Param("labelCode") String labelCode,
                                  @Param("orderCode") String orderCode);

    // Delete operations
    void deleteById(Long id);
    void deleteByOrderId(Long orderId);
    void deleteByOrderBoxId(Long orderBoxId);
    void deleteOldScanEvents(@Param("cutoffDate") LocalDateTime cutoffDate);
}