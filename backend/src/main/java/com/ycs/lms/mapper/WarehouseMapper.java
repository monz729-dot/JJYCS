package com.ycs.lms.mapper;

import com.ycs.lms.entity.OrderBox;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface WarehouseMapper {

    @Select("SELECT * FROM warehouses WHERE id = #{id}")
    Map<String, Object> findById(@Param("id") Long id);

    @Select("SELECT * FROM warehouses WHERE status = 'active'")
    List<Map<String, Object>> findActiveWarehouses();

    @Update("UPDATE warehouses SET status = #{status} WHERE id = #{id}")
    int updateWarehouseStatus(@Param("id") Long id, @Param("status") String status);

    @Select("SELECT * FROM inventory WHERE warehouse_id = #{warehouseId} ORDER BY created_at DESC")
    List<Map<String, Object>> findInventoryByWarehouse(@Param("warehouseId") Long warehouseId);

    @Insert("INSERT INTO scan_events (order_id, label_code, scan_type, warehouse_id, scanned_by, scanned_at) " +
            "VALUES (#{orderId}, #{labelCode}, #{scanType}, #{warehouseId}, #{scannedBy}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertScanEvent(@Param("orderId") Long orderId, 
                       @Param("labelCode") String labelCode,
                       @Param("scanType") String scanType,
                       @Param("warehouseId") Long warehouseId,
                       @Param("scannedBy") Long scannedBy);

    @Select("SELECT * FROM scan_events WHERE warehouse_id = #{warehouseId} ORDER BY scanned_at DESC")
    List<Map<String, Object>> findScanEventsByWarehouse(@Param("warehouseId") Long warehouseId);
}