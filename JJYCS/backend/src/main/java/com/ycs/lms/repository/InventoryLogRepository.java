package com.ycs.lms.repository;

import com.ycs.lms.entity.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
    
    List<InventoryLog> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    List<InventoryLog> findByActionTypeOrderByCreatedAtDesc(InventoryLog.ActionType actionType);
    
    List<InventoryLog> findByPerformedByOrderByCreatedAtDesc(String performedBy);
    
    @Query("SELECT i FROM InventoryLog i WHERE i.createdAt BETWEEN :startDate AND :endDate ORDER BY i.createdAt DESC")
    List<InventoryLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT i FROM InventoryLog i WHERE i.newLocation = :location OR i.previousLocation = :location ORDER BY i.createdAt DESC")
    List<InventoryLog> findByLocation(@Param("location") String location);
}