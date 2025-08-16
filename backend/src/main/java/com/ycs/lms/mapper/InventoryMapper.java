package com.ycs.lms.mapper;

import com.ycs.lms.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 재고 MyBatis 매퍼 인터페이스
 * XML 파일에서 구현됨: InventoryMapper.xml
 */
@Mapper
public interface InventoryMapper {

    // Inventory CRUD operations
    void insertInventory(Inventory inventory);
    void insertInventories(List<Inventory> inventories);
    Inventory findById(Long id);
    
    // Search operations
    List<Inventory> findByWarehouseId(Long warehouseId);
    List<Inventory> findByLocation(String location);
    List<Inventory> findByLocationPattern(String locationPattern);
    List<Inventory> findByOrderId(Long orderId);
    List<Inventory> findByOrderBoxId(Long orderBoxId);
    List<Inventory> findByStatus(String status);
    List<Inventory> findByWarehouseIdAndStatus(@Param("warehouseId") Long warehouseId, @Param("status") String status);
    List<Inventory> findByWarehouseIdAndLocation(@Param("warehouseId") Long warehouseId, @Param("location") String location);
    
    // Advanced search
    List<Inventory> findAvailableByWarehouseId(Long warehouseId);
    List<Inventory> findByLocationRange(@Param("warehouseId") Long warehouseId, 
                                       @Param("startLocation") String startLocation, 
                                       @Param("endLocation") String endLocation);
    List<Inventory> findExpiredInventory(@Param("warehouseId") Long warehouseId, @Param("daysOld") int daysOld);
    
    // Aggregate operations
    long countByWarehouseId(Long warehouseId);
    long countByWarehouseIdAndStatus(@Param("warehouseId") Long warehouseId, @Param("status") String status);
    long countByLocation(String location);
    
    // Update operations
    void updateInventory(Inventory inventory);
    void updateInventoryStatus(@Param("id") Long id, @Param("status") String status);
    void updateInventoryLocation(@Param("id") Long id, @Param("location") String location);
    void updateInventoryLocationBatch(@Param("ids") List<Long> ids, @Param("location") String location);
    
    // Delete operations
    void deleteById(Long id);
    void deleteByWarehouseId(Long warehouseId);
    void deleteByOrderId(Long orderId);
    void deleteByOrderBoxId(Long orderBoxId);
}