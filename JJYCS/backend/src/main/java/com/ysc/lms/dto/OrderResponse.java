package com.ysc.lms.dto;

import com.ysc.lms.entity.Order;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 응답 DTO - MVP용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    
    private Long id;
    private String orderNo;
    private Long userId;
    private String status;
    private String statusDisplay;
    private BigDecimal totalAmount;
    private String currency;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<OrderItemResponse> items = new ArrayList<>();
    
    // 사용자 정보 (간소화)
    private String userEmail;
    private String userName;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long orderId;
        private Long productId;
        private String name;
        private Integer qty;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    // Entity를 DTO로 변환하는 편의 메서드
    public static OrderResponse from(Order order) {
        OrderResponseBuilder builder = OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNumber())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .statusDisplay(order.getStatus().getDisplayName())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt());
        
        // User 정보가 있으면 설정
        if (order.getUser() != null) {
            builder.userEmail(order.getUser().getEmail())
                   .userName(order.getUser().getName());
        }
        
        // OrderItems 변환
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                    .map(item -> OrderItemResponse.builder()
                            .id(item.getId())
                            .orderId(item.getOrderId())
                            .productId(item.getProductId())
                            .name(item.getName())
                            .qty(item.getQty())
                            .unitPrice(item.getUnitPrice())
                            .amount(item.getAmount())
                            .createdAt(item.getCreatedAt())
                            .updatedAt(item.getUpdatedAt())
                            .build())
                    .toList();
            builder.items(itemResponses);
        }
        
        return builder.build();
    }
}