package com.ycs.lms.mapper;

import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.OrderBox;
import com.ycs.lms.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    // Order CRUD operations
    @Insert("""
        INSERT INTO orders (order_code, user_id, status, order_type, recipient_name, recipient_phone, 
                           recipient_address, recipient_zip_code, recipient_country, urgency, needs_repacking, 
                           special_instructions, total_amount, currency, requires_extra_recipient, 
                           payment_method, payment_status, created_by)
        VALUES (#{orderCode}, #{userId}, #{status}, #{orderType}, #{recipientName}, #{recipientPhone}, 
                #{recipientAddress}, #{recipientZipCode}, #{recipientCountry}, #{urgency}, #{needsRepacking}, 
                #{specialInstructions}, #{totalAmount}, #{currency}, #{requiresExtraRecipient}, 
                #{paymentMethod}, #{paymentStatus}, #{createdBy})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertOrder(Order order);

    @Select("""
        SELECT o.*, u.name as user_name, u.email as user_email, u.member_code as user_member_code, u.role as user_role
        FROM orders o
        JOIN users u ON o.user_id = u.id
        WHERE o.id = #{orderId}
    """)
    @Results(id = "orderResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "orderCode", column = "order_code"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "orderType", column = "order_type"),
        @Result(property = "recipientName", column = "recipient_name"),
        @Result(property = "recipientPhone", column = "recipient_phone"),
        @Result(property = "recipientAddress", column = "recipient_address"),
        @Result(property = "recipientZipCode", column = "recipient_zip_code"),
        @Result(property = "recipientCountry", column = "recipient_country"),
        @Result(property = "needsRepacking", column = "needs_repacking"),
        @Result(property = "specialInstructions", column = "special_instructions"),
        @Result(property = "totalAmount", column = "total_amount"),
        @Result(property = "totalCbmM3", column = "total_cbm_m3"),
        @Result(property = "requiresExtraRecipient", column = "requires_extra_recipient"),
        @Result(property = "estimatedDeliveryDate", column = "estimated_delivery_date"),
        @Result(property = "actualDeliveryDate", column = "actual_delivery_date"),
        @Result(property = "paymentMethod", column = "payment_method"),
        @Result(property = "paymentStatus", column = "payment_status"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "assignedWarehouseId", column = "assigned_warehouse_id"),
        @Result(property = "estimatedCost", column = "estimated_cost"),
        @Result(property = "actualCost", column = "actual_cost"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Order findOrderById(Long orderId);

    /**
     * N+1 문제 해결을 위한 최적화된 주문 조회 (items와 boxes 포함)
     */
    @Select("""
        SELECT 
            o.*, 
            u.name as user_name, u.email as user_email, u.member_code as user_member_code, u.role as user_role,
            oi.id as item_id, oi.item_order, oi.name as item_name, oi.description as item_description,
            oi.category, oi.quantity, oi.unit_weight, oi.unit_price, oi.total_amount as item_total_amount,
            oi.currency as item_currency, oi.hs_code, oi.ems_code, oi.country_of_origin, oi.brand, oi.model,
            oi.restriction_note, oi.created_at as item_created_at, oi.updated_at as item_updated_at,
            ob.id as box_id, ob.box_number, ob.label_code, ob.qr_code_url, 
            ob.width_cm, ob.height_cm, ob.depth_cm, ob.cbm_m3, ob.weight_kg,
            ob.warehouse_id, ob.warehouse_location, ob.inbound_date, ob.outbound_date,
            ob.tracking_number, ob.shipped_date, ob.status as box_status, ob.item_ids,
            ob.created_at as box_created_at, ob.updated_at as box_updated_at
        FROM orders o
        JOIN users u ON o.user_id = u.id
        LEFT JOIN order_items oi ON o.id = oi.order_id
        LEFT JOIN order_boxes ob ON o.id = ob.order_id
        WHERE o.id = #{orderId}
        ORDER BY oi.item_order, ob.box_number
    """)
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "orderCode", column = "order_code"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "orderType", column = "order_type"),
        @Result(property = "recipientName", column = "recipient_name"),
        @Result(property = "recipientPhone", column = "recipient_phone"),
        @Result(property = "recipientAddress", column = "recipient_address"),
        @Result(property = "recipientZipCode", column = "recipient_zip_code"),
        @Result(property = "recipientCountry", column = "recipient_country"),
        @Result(property = "needsRepacking", column = "needs_repacking"),
        @Result(property = "specialInstructions", column = "special_instructions"),
        @Result(property = "totalAmount", column = "total_amount"),
        @Result(property = "totalCbmM3", column = "total_cbm_m3"),
        @Result(property = "requiresExtraRecipient", column = "requires_extra_recipient"),
        @Result(property = "estimatedDeliveryDate", column = "estimated_delivery_date"),
        @Result(property = "actualDeliveryDate", column = "actual_delivery_date"),
        @Result(property = "paymentMethod", column = "payment_method"),
        @Result(property = "paymentStatus", column = "payment_status"),
        @Result(property = "createdBy", column = "created_by"),
        @Result(property = "assignedWarehouseId", column = "assigned_warehouse_id"),
        @Result(property = "estimatedCost", column = "estimated_cost"),
        @Result(property = "actualCost", column = "actual_cost"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Order findOrderByIdWithDetails(Long orderId);

    @Select("""
        <script>
        SELECT o.*, u.name as user_name, u.email as user_email, u.member_code as user_member_code, u.role as user_role
        FROM orders o
        JOIN users u ON o.user_id = u.id
        WHERE 1=1
        <if test="userId != null">
            AND o.user_id = #{userId}
        </if>
        <if test="status != null and status != 'all'">
            AND o.status = #{status}
        </if>
        <if test="startDate != null">
            AND o.created_at >= #{startDate}
        </if>
        <if test="endDate != null">
            AND o.created_at <= #{endDate}
        </if>
        ORDER BY o.created_at DESC
        LIMIT #{limit} OFFSET #{offset}
        </script>
    """)
    @ResultMap("orderResultMap")
    List<Order> findOrders(@Param("userId") Long userId, @Param("status") String status, 
                          @Param("startDate") String startDate, @Param("endDate") String endDate,
                          @Param("limit") int limit, @Param("offset") int offset);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM orders o
        WHERE 1=1
        <if test="userId != null">
            AND o.user_id = #{userId}
        </if>
        <if test="status != null and status != 'all'">
            AND o.status = #{status}
        </if>
        <if test="startDate != null">
            AND o.created_at >= #{startDate}
        </if>
        <if test="endDate != null">
            AND o.created_at <= #{endDate}
        </if>
        </script>
    """)
    long countOrders(@Param("userId") Long userId, @Param("status") String status, 
                    @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Update("""
        UPDATE orders SET status = #{status}, order_type = #{orderType}, total_cbm_m3 = #{totalCbmM3}, 
                         requires_extra_recipient = #{requiresExtraRecipient}, updated_at = NOW()
        WHERE id = #{id}
    """)
    void updateOrder(Order order);

    @Update("""
        UPDATE orders SET status = 'cancelled', updated_at = NOW() WHERE id = #{orderId}
    """)
    void cancelOrder(Long orderId);

    // OrderItem operations
    @Insert("""
        INSERT INTO order_items (order_id, item_order, name, description, category, quantity, unit_weight, 
                                unit_price, total_amount, currency, hs_code, ems_code, country_of_origin, brand, model)
        VALUES (#{orderId}, #{itemOrder}, #{name}, #{description}, #{category}, #{quantity}, #{unitWeight}, 
                #{unitPrice}, #{totalAmount}, #{currency}, #{hsCode}, #{emsCode}, #{countryOfOrigin}, #{brand}, #{model})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertOrderItem(OrderItem item);

    @Select("""
        SELECT * FROM order_items WHERE order_id = #{orderId} ORDER BY item_order
    """)
    @Results({
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "itemOrder", column = "item_order"),
        @Result(property = "unitWeight", column = "unit_weight"),
        @Result(property = "unitPrice", column = "unit_price"),
        @Result(property = "totalAmount", column = "total_amount"),
        @Result(property = "hsCode", column = "hs_code"),
        @Result(property = "emsCode", column = "ems_code"),
        @Result(property = "countryOfOrigin", column = "country_of_origin"),
        @Result(property = "restrictionNote", column = "restriction_note"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<OrderItem> findOrderItemsByOrderId(Long orderId);

    // OrderBox operations
    @Insert("""
        INSERT INTO order_boxes (order_id, box_number, label_code, width_cm, height_cm, depth_cm, 
                               weight_kg, status, item_ids)
        VALUES (#{orderId}, #{boxNumber}, #{labelCode}, #{widthCm}, #{heightCm}, #{depthCm}, 
                #{weightKg}, #{status}, #{itemIdsJson})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertOrderBox(OrderBox box);

    @Select("""
        SELECT * FROM order_boxes WHERE order_id = #{orderId} ORDER BY box_number
    """)
    @Results({
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "boxNumber", column = "box_number"),
        @Result(property = "labelCode", column = "label_code"),
        @Result(property = "qrCodeUrl", column = "qr_code_url"),
        @Result(property = "widthCm", column = "width_cm"),
        @Result(property = "heightCm", column = "height_cm"),
        @Result(property = "depthCm", column = "depth_cm"),
        @Result(property = "cbmM3", column = "cbm_m3"),
        @Result(property = "weightKg", column = "weight_kg"),
        @Result(property = "warehouseId", column = "warehouse_id"),
        @Result(property = "warehouseLocation", column = "warehouse_location"),
        @Result(property = "inboundDate", column = "inbound_date"),
        @Result(property = "outboundDate", column = "outbound_date"),
        @Result(property = "trackingNumber", column = "tracking_number"),
        @Result(property = "shippedDate", column = "shipped_date"),
        @Result(property = "itemIds", column = "item_ids"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<OrderBox> findOrderBoxesByOrderId(Long orderId);

    @Select("""
        SELECT * FROM order_boxes WHERE label_code = #{labelCode}
    """)
    @Results({
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "boxNumber", column = "box_number"),
        @Result(property = "labelCode", column = "label_code"),
        @Result(property = "qrCodeUrl", column = "qr_code_url"),
        @Result(property = "widthCm", column = "width_cm"),
        @Result(property = "heightCm", column = "height_cm"),
        @Result(property = "depthCm", column = "depth_cm"),
        @Result(property = "cbmM3", column = "cbm_m3"),
        @Result(property = "weightKg", column = "weight_kg"),
        @Result(property = "warehouseId", column = "warehouse_id"),
        @Result(property = "warehouseLocation", column = "warehouse_location"),
        @Result(property = "inboundDate", column = "inbound_date"),
        @Result(property = "outboundDate", column = "outbound_date"),
        @Result(property = "trackingNumber", column = "tracking_number"),
        @Result(property = "shippedDate", column = "shipped_date"),
        @Result(property = "itemIds", column = "item_ids"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    OrderBox findOrderBoxByLabelCode(String labelCode);

    @Update("""
        UPDATE order_boxes SET status = #{status}, warehouse_id = #{warehouseId}, 
                              warehouse_location = #{warehouseLocation}, updated_at = NOW()
        WHERE id = #{id}
    """)
    void updateOrderBoxStatus(OrderBox box);

    // Business logic queries
    @Select("""
        SELECT SUM(cbm_m3) FROM order_boxes WHERE order_id = #{orderId}
    """)
    java.math.BigDecimal getTotalCbmByOrderId(Long orderId);

    @Select("""
        SELECT SUM(total_amount) FROM order_items WHERE order_id = #{orderId}
    """)
    java.math.BigDecimal getTotalAmountByOrderId(Long orderId);

    @Select("""
        SELECT COUNT(*) > 0 FROM users WHERE id = #{userId} AND (member_code IS NULL OR member_code = '')
    """)
    boolean hasMemberCodeIssue(Long userId);

    @Select("""
        SELECT CONCAT('ORD-', YEAR(NOW()), '-', LPAD(COALESCE(MAX(CAST(SUBSTRING(order_code, 10) AS UNSIGNED)), 0) + 1, 3, '0'))
        FROM orders 
        WHERE order_code LIKE CONCAT('ORD-', YEAR(NOW()), '-%')
    """)
    String generateOrderCode();

    @Select("""
        SELECT CONCAT('BOX-', YEAR(NOW()), '-', LPAD(#{orderSeq}, 3, '0'), '-', LPAD(#{boxNumber}, 2, '0'))
    """)
    String generateBoxLabelCode(@Param("orderSeq") int orderSeq, @Param("boxNumber") int boxNumber);
}