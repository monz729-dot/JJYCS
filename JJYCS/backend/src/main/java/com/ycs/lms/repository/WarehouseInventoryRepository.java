package com.ycs.lms.repository;

import com.ycs.lms.entity.WarehouseInventory;
import com.ycs.lms.entity.WarehouseInventory.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Long> {
    
    /**
     * 주문 ID로 창고 재고 조회
     */
    List<WarehouseInventory> findByOrderId(Long orderId);
    
    /**
     * 창고 ID로 재고 조회
     */
    List<WarehouseInventory> findByWarehouseId(Long warehouseId);
    
    /**
     * 재고 코드로 조회
     */
    Optional<WarehouseInventory> findByInventoryCode(String inventoryCode);
    
    /**
     * 바코드로 조회
     */
    Optional<WarehouseInventory> findByBarcode(String barcode);
    
    /**
     * QR 코드로 조회
     */
    Optional<WarehouseInventory> findByQrCode(String qrCode);
    
    /**
     * 상태별 재고 조회
     */
    List<WarehouseInventory> findByStatus(InventoryStatus status);
    
    /**
     * 창고별 상태별 재고 조회
     */
    List<WarehouseInventory> findByWarehouseIdAndStatus(Long warehouseId, InventoryStatus status);
    
    /**
     * 출고 준비 완료된 재고 목록
     */
    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.warehouse.id = :warehouseId AND wi.status = 'READY_FOR_SHIPPING' ORDER BY wi.receivedAt ASC")
    List<WarehouseInventory> findReadyForShipping(@Param("warehouseId") Long warehouseId);
    
    /**
     * 검수가 필요한 재고 목록
     */
    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.warehouse.id = :warehouseId AND wi.status IN ('RECEIVED', 'INSPECTING') ORDER BY wi.receivedAt ASC")
    List<WarehouseInventory> findRequiresInspection(@Param("warehouseId") Long warehouseId);
    
    /**
     * 보류 상태 재고 목록
     */
    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.warehouse.id = :warehouseId AND wi.status = 'ON_HOLD' ORDER BY wi.receivedAt ASC")
    List<WarehouseInventory> findOnHold(@Param("warehouseId") Long warehouseId);
    
    /**
     * 손상/분실 재고 목록
     */
    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.warehouse.id = :warehouseId AND wi.status IN ('DAMAGED', 'MISSING') ORDER BY wi.receivedAt DESC")
    List<WarehouseInventory> findDamagedOrMissing(@Param("warehouseId") Long warehouseId);
    
    /**
     * 기간별 입고 통계
     */
    @Query("SELECT COUNT(wi) FROM WarehouseInventory wi WHERE wi.warehouse.id = :warehouseId AND wi.receivedAt BETWEEN :startDate AND :endDate")
    Long countReceived(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 기간별 출고 통계
     */
    @Query("SELECT COUNT(wi) FROM WarehouseInventory wi WHERE wi.warehouse.id = :warehouseId AND wi.shippedAt BETWEEN :startDate AND :endDate")
    Long countShipped(@Param("warehouseId") Long warehouseId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 창고별 상태별 재고 수량 통계
     */
    @Query("SELECT wi.status, COUNT(wi) FROM WarehouseInventory wi WHERE wi.warehouse.id = :warehouseId GROUP BY wi.status")
    List<Object[]> countByStatusAndWarehouse(@Param("warehouseId") Long warehouseId);
    
    /**
     * 리패킹이 필요한 재고 목록
     */
    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.repackRequired = true AND wi.status = 'INSPECTION_COMPLETE' ORDER BY wi.inspectedAt ASC")
    List<WarehouseInventory> findRequiresRepacking();
    
    /**
     * 특정 위치의 재고 조회
     */
    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.warehouse.id = :warehouseId AND wi.locationZone = :zone AND wi.locationShelf = :shelf AND wi.locationPosition = :position")
    List<WarehouseInventory> findByLocation(@Param("warehouseId") Long warehouseId, @Param("zone") String zone, @Param("shelf") String shelf, @Param("position") String position);
    
    /**
     * 최근 입고된 재고 목록 (대시보드용)
     */
    @Query("SELECT wi FROM WarehouseInventory wi WHERE wi.warehouse.id = :warehouseId AND wi.receivedAt IS NOT NULL ORDER BY wi.receivedAt DESC")
    List<WarehouseInventory> findRecentReceived(@Param("warehouseId") Long warehouseId, org.springframework.data.domain.Pageable pageable);
    
    /**
     * 재고 코드 중복 확인
     */
    boolean existsByInventoryCode(String inventoryCode);
}