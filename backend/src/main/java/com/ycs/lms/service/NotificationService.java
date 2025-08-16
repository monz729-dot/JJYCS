package com.ycs.lms.service;

import com.ycs.lms.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 알림 서비스
 * - 이메일 알림
 * - SMS 알림
 * - 푸시 알림
 */
@Service
@Slf4j
public class NotificationService {

    /**
     * 주문 생성 알림 발송
     */
    public void sendOrderCreatedNotification(Order order) {
        log.info("Sending order created notification for order: {}", order.getOrderCode());
        
        // TODO: 실제 알림 발송 로직 구현
        // - 이메일 발송
        // - SMS 발송 (선택적)
        // - 푸시 알림 (선택적)
    }

    /**
     * 지연 주문 알림 발송 (관리자용)
     */
    public void sendDelayedOrderAlert(Order order) {
        log.info("Sending delayed order alert for order: {}", order.getOrderCode());
        
        // TODO: 관리자에게 지연 주문 알림 발송
        // - 관리자 이메일로 알림
        // - 관리자 대시보드에 알림 표시
    }

    /**
     * 주문 상태 변경 알림
     */
    public void sendOrderStatusChangeNotification(Order order, String oldStatus, String newStatus) {
        log.info("Sending order status change notification: {} -> {} for order: {}", 
                oldStatus, newStatus, order.getOrderCode());
        
        // TODO: 주문 상태 변경 알림 발송
    }

    /**
     * 배송 시작 알림
     */
    public void sendShippingStartedNotification(Order order) {
        log.info("Sending shipping started notification for order: {}", order.getOrderCode());
        
        // TODO: 배송 시작 알림 발송
    }

    /**
     * 배송 완료 알림
     */
    public void sendShippingCompletedNotification(Order order) {
        log.info("Sending shipping completed notification for order: {}", order.getOrderCode());
        
        // TODO: 배송 완료 알림 발송
    }
} 