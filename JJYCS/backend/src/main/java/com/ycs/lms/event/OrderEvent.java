package com.ycs.lms.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonTypeName("ORDER_EVENT")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrderEvent extends BaseEvent {
    
    private Long orderId;
    private String orderNumber;
    private String action; // CREATED, UPDATED, STATUS_CHANGED, CANCELLED, SHIPPED, DELIVERED
    private String previousStatus;
    private String currentStatus;
    private Double totalAmount;
    private String currency;
    private Object orderData; // 전체 주문 데이터 (필요시)

    public OrderEvent(String action, Long orderId, String orderNumber) {
        super("ORDER_EVENT", "order-service");
        this.action = action;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
    }

    public static OrderEvent created(Long orderId, String orderNumber, String userId) {
        OrderEvent event = new OrderEvent("CREATED", orderId, orderNumber);
        event.setUserId(userId);
        return event;
    }

    public static OrderEvent statusChanged(Long orderId, String orderNumber, 
                                         String previousStatus, String currentStatus, String userId) {
        OrderEvent event = new OrderEvent("STATUS_CHANGED", orderId, orderNumber);
        event.setPreviousStatus(previousStatus);
        event.setCurrentStatus(currentStatus);
        event.setUserId(userId);
        return event;
    }

    public static OrderEvent cancelled(Long orderId, String orderNumber, String userId) {
        OrderEvent event = new OrderEvent("CANCELLED", orderId, orderNumber);
        event.setCurrentStatus("CANCELLED");
        event.setUserId(userId);
        return event;
    }

    public static OrderEvent shipped(Long orderId, String orderNumber, String userId) {
        OrderEvent event = new OrderEvent("SHIPPED", orderId, orderNumber);
        event.setCurrentStatus("SHIPPED");
        event.setUserId(userId);
        return event;
    }

    public static OrderEvent delivered(Long orderId, String orderNumber, String userId) {
        OrderEvent event = new OrderEvent("DELIVERED", orderId, orderNumber);
        event.setCurrentStatus("DELIVERED");
        event.setUserId(userId);
        return event;
    }
}