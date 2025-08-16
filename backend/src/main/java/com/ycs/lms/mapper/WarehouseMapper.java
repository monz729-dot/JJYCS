package com.ycs.lms.mapper;

import com.ycs.lms.entity.OrderBox;
import org.apache.ibatis.annotations.*;

import java.util.List;

// @Mapper - 일시적으로 비활성화
public interface WarehouseMapper {

    // Scan Events
    @Insert("""
        INSERT INTO scan_events (event_type, box_id, warehouse_id, scanned_by, previous_status, new_status, 
                                location_code, batch_id, device_info, notes, scan_timestamp)
        VALUES (#{eventType}, #{boxId}, #{warehouseId}, #{scannedBy}, #{previousStatus}, #{newStatus},
                #{locationCode}, #{batchId}, #{deviceInfo}, #{notes}, #{scanTimestamp})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertScanEvent(@Param("eventType") String eventType, @Param("boxId") Long boxId, 
                        @Param("warehouseId") Long warehouseId, @Param("scannedBy") Long scannedBy,
                        @Param("previousStatus") String previousStatus, @Param("newStatus") String newStatus,
                        @Param("locationCode") String locationCode, @Param("batchId") String batchId,
                        @Param("deviceInfo") String deviceInfo, @Param("notes") String notes,
                        @Param("scanTimestamp") java.time.LocalDateTime scanTimestamp);

    @Select("""
        <script>
        SELECT se.*, ob.label_code, ob.order_id, o.order_code, 
               u.name as scanned_by_name, u.role as scanned_by_role,
               w.name as warehouse_name, w.code as warehouse_code
        FROM scan_events se
        JOIN order_boxes ob ON se.box_id = ob.id
        JOIN orders o ON ob.order_id = o.id
        JOIN users u ON se.scanned_by = u.id
        JOIN warehouses w ON se.warehouse_id = w.id
        WHERE 1=1
        <if test="labelCode != null">
            AND ob.label_code = #{labelCode}
        </if>
        <if test="startDate != null">
            AND se.scan_timestamp >= #{startDate}
        </if>
        <if test="endDate != null">
            AND se.scan_timestamp <= #{endDate}
        </if>
        ORDER BY se.scan_timestamp DESC
        LIMIT #{limit} OFFSET #{offset}
        </script>
    """)
    @Results({
        @Result(property = "eventType", column = "event_type"),
        @Result(property = "boxId", column = "box_id"),
        @Result(property = "warehouseId", column = "warehouse_id"),
        @Result(property = "scannedBy", column = "scanned_by"),
        @Result(property = "previousStatus", column = "previous_status"),
        @Result(property = "newStatus", column = "new_status"),
        @Result(property = "locationCode", column = "location_code"),
        @Result(property = "batchId", column = "batch_id"),
        @Result(property = "deviceInfo", column = "device_info"),
        @Result(property = "scanTimestamp", column = "scan_timestamp"),
        @Result(property = "labelCode", column = "label_code"),
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "orderCode", column = "order_code"),
        @Result(property = "scannedByName", column = "scanned_by_name"),
        @Result(property = "scannedByRole", column = "scanned_by_role"),
        @Result(property = "warehouseName", column = "warehouse_name"),
        @Result(property = "warehouseCode", column = "warehouse_code")
    })
    List<java.util.Map<String, Object>> findScanEvents(@Param("labelCode") String labelCode,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate,
                                                       @Param("limit") int limit, @Param("offset") int offset);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM scan_events se
        JOIN order_boxes ob ON se.box_id = ob.id
        WHERE 1=1
        <if test="labelCode != null">
            AND ob.label_code = #{labelCode}
        </if>
        <if test="startDate != null">
            AND se.scan_timestamp >= #{startDate}
        </if>
        <if test="endDate != null">
            AND se.scan_timestamp <= #{endDate}
        </if>
        </script>
    """)
    long countScanEvents(@Param("labelCode") String labelCode, @Param("startDate") String startDate, @Param("endDate") String endDate);

    // Inventory
    @Select("""
        <script>
        SELECT i.*, ob.label_code, ob.cbm_m3, ob.weight_kg, o.id as order_id, o.order_code,
               u.name as customer_name, u.member_code, u.role as customer_role,
               w.name as warehouse_name
        FROM inventory i
        JOIN order_boxes ob ON i.box_id = ob.id
        JOIN orders o ON ob.order_id = o.id
        JOIN users u ON o.user_id = u.id
        JOIN warehouses w ON i.warehouse_id = w.id
        WHERE i.warehouse_id = #{warehouseId}
        <if test="status != null and status != 'all'">
            AND i.status = #{status}
        </if>
        ORDER BY i.inbound_date DESC
        LIMIT #{limit} OFFSET #{offset}
        </script>
    """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "warehouseId", column = "warehouse_id"),
        @Result(property = "boxId", column = "box_id"),
        @Result(property = "locationCode", column = "location_code"),
        @Result(property = "status", column = "status"),
        @Result(property = "inboundDate", column = "inbound_date"),
        @Result(property = "expectedOutboundDate", column = "expected_outbound_date"),
        @Result(property = "lastScannedAt", column = "last_scanned_at"),
        @Result(property = "daysInWarehouse", column = "days_in_warehouse"),
        @Result(property = "labelCode", column = "label_code"),
        @Result(property = "cbmM3", column = "cbm_m3"),
        @Result(property = "weightKg", column = "weight_kg"),
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "orderCode", column = "order_code"),
        @Result(property = "customerName", column = "customer_name"),
        @Result(property = "memberCode", column = "member_code"),
        @Result(property = "customerRole", column = "customer_role"),
        @Result(property = "warehouseName", column = "warehouse_name")
    })
    List<java.util.Map<String, Object>> findInventory(@Param("warehouseId") Long warehouseId,
                                                      @Param("status") String status,
                                                      @Param("limit") int limit, @Param("offset") int offset);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM inventory i
        WHERE i.warehouse_id = #{warehouseId}
        <if test="status != null and status != 'all'">
            AND i.status = #{status}
        </if>
        </script>
    """)
    long countInventory(@Param("warehouseId") Long warehouseId, @Param("status") String status);

    @Select("""
        SELECT status, COUNT(*) as count
        FROM inventory
        WHERE warehouse_id = #{warehouseId}
        GROUP BY status
    """)
    List<java.util.Map<String, Object>> getInventoryStatusCounts(@Param("warehouseId") Long warehouseId);

    // Warehouse info
    @Select("""
        SELECT * FROM warehouses WHERE id = #{warehouseId} AND is_active = TRUE
    """)
    @Results({
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "capacityDescription", column = "capacity_description"),
        @Result(property = "operatingHours", column = "operating_hours"),
        @Result(property = "contactInfo", column = "contact_info"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    java.util.Map<String, Object> findWarehouseById(@Param("warehouseId") Long warehouseId);

    // Box status updates
    @Update("""
        UPDATE order_boxes 
        SET status = #{status}, warehouse_id = #{warehouseId}, warehouse_location = #{location}, updated_at = NOW()
        WHERE label_code = #{labelCode}
    """)
    int updateBoxStatus(@Param("labelCode") String labelCode, @Param("status") String status,
                       @Param("warehouseId") Long warehouseId, @Param("location") String location);

    @Update("""
        UPDATE order_boxes 
        SET status = #{status}, updated_at = NOW()
        WHERE label_code = #{labelCode}
    """)
    int updateBoxStatusOnly(@Param("labelCode") String labelCode, @Param("status") String status);

    // Inventory management
    @Insert("""
        INSERT INTO inventory (warehouse_id, box_id, location_code, status, inbound_date)
        VALUES (#{warehouseId}, #{boxId}, #{locationCode}, #{status}, NOW())
        ON DUPLICATE KEY UPDATE
        location_code = VALUES(location_code),
        status = VALUES(status),
        updated_at = NOW()
    """)
    void upsertInventory(@Param("warehouseId") Long warehouseId, @Param("boxId") Long boxId,
                        @Param("locationCode") String locationCode, @Param("status") String status);

    @Update("""
        UPDATE inventory 
        SET status = #{status}, outbound_date = CASE WHEN #{status} = 'shipped' THEN NOW() ELSE outbound_date END, updated_at = NOW()
        WHERE warehouse_id = #{warehouseId} AND box_id = #{boxId}
    """)
    void updateInventoryStatus(@Param("warehouseId") Long warehouseId, @Param("boxId") Long boxId, @Param("status") String status);

    // Batch processing
    @Select("""
        SELECT ob.*, o.order_code, o.user_id
        FROM order_boxes ob
        JOIN orders o ON ob.order_id = o.id
        WHERE ob.label_code IN 
        <foreach item="labelCode" collection="labelCodes" open="(" separator="," close=")">
            #{labelCode}
        </foreach>
    """)
    List<OrderBox> findBoxesByLabelCodes(@Param("labelCodes") List<String> labelCodes);

    // QR Code and Label generation
    @Update("""
        UPDATE order_boxes 
        SET qr_code_url = #{qrCodeUrl}, updated_at = NOW()
        WHERE id = #{boxId}
    """)
    void updateBoxQrCode(@Param("boxId") Long boxId, @Param("qrCodeUrl") String qrCodeUrl);

    @Select("""
        SELECT CONCAT('BATCH-', YEAR(NOW()), '-', MONTH(NOW()), '-', DAY(NOW()), '-', 
               LPAD(COALESCE(MAX(CAST(SUBSTRING_INDEX(batch_id, '-', -1) AS UNSIGNED)), 0) + 1, 3, '0'))
        FROM scan_events 
        WHERE batch_id LIKE CONCAT('BATCH-', YEAR(NOW()), '-', MONTH(NOW()), '-', DAY(NOW()), '-%')
        AND scan_timestamp >= CURDATE()
    """)
    String generateBatchId();
}