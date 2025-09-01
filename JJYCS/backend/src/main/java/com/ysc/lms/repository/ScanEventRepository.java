package com.ysc.lms.repository;

import com.ysc.lms.entity.ScanEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScanEventRepository extends JpaRepository<ScanEvent, Long> {
    
    List<ScanEvent> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    List<ScanEvent> findByScanTypeOrderByCreatedAtDesc(ScanEvent.ScanType scanType);
    
    List<ScanEvent> findByProcessedOrderByCreatedAtDesc(Boolean processed);
    
    Optional<ScanEvent> findByScanCode(String scanCode);
    
    @Query("SELECT s FROM ScanEvent s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<ScanEvent> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM ScanEvent s WHERE s.scannedBy = :scannedBy AND s.createdAt >= :startDate ORDER BY s.createdAt DESC")
    List<ScanEvent> findByScannedByAndDateAfter(@Param("scannedBy") String scannedBy, 
                                               @Param("startDate") LocalDateTime startDate);
    
    long countByProcessed(Boolean processed);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM ScanEvent s WHERE s.order.orderNumber = :orderNumber AND s.scanType = :scanType")
    boolean existsByOrderNumberAndScanType(@Param("orderNumber") String orderNumber, @Param("scanType") ScanEvent.ScanType scanType);
}