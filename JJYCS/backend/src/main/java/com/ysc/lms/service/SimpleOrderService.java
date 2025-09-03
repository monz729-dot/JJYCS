package com.ysc.lms.service;

import com.ysc.lms.dto.SimpleCreateOrderRequest;
import com.ysc.lms.entity.Order;
import com.ysc.lms.entity.OrderItem;
import com.ysc.lms.exception.BusinessRuleViolationException;
import com.ysc.lms.exception.ResourceNotFoundException;
import com.ysc.lms.repository.OrderRepository;
import com.ysc.lms.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 간단한 주문 서비스 - MVP용
 * 트랜잭션 관리, 주문번호 생성, 서버 계산 검증
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleOrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    
    /**
     * 주문 생성 - 트랜잭션 처리
     */
    @Transactional
    public Order createOrder(Long userId, SimpleCreateOrderRequest request) {
        
        // 1. 사용자 DRAFT 주문 개수 제한 체크 (최대 3개)
        long draftCount = orderRepository.countDraftOrdersByUserId(userId);
        if (draftCount >= 3) {
            throw new BusinessRuleViolationException(
                "임시저장 주문은 최대 3개까지만 가능합니다. 기존 주문을 제출하거나 삭제 후 다시 시도해주세요.");
        }
        
        // 2. 주문번호 생성 (동시성 처리)
        String orderNo = generateOrderNumber();
        
        // 3. 서버에서 총액 재계산 및 검증
        BigDecimal serverCalculatedTotal = calculateTotalAmount(request.getItems());
        if (request.getTotalAmount() != null && 
            serverCalculatedTotal.compareTo(request.getTotalAmount()) != 0) {
            log.warn("클라이언트 총액과 서버 계산 총액 불일치: 클라이언트={}, 서버={}", 
                request.getTotalAmount(), serverCalculatedTotal);
        }
        
        // 4. 주문 엔티티 생성
        Order order = new Order();
        order.setOrderNumber(orderNo);
        order.setUserId(userId);
        order.setStatus(Order.OrderStatus.DRAFT);
        order.setTotalAmount(serverCalculatedTotal); // 서버 계산 값 사용
        order.setCurrency(request.getCurrency() != null ? request.getCurrency() : "KRW");
        order.setNote(request.getNote());
        
        // 5. 주문 저장
        order = orderRepository.save(order);
        log.info("주문 생성: ID={}, 주문번호={}, 총액={}", order.getId(), orderNo, serverCalculatedTotal);
        
        // 6. 주문 아이템들 생성
        for (SimpleCreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(itemReq.getProductId());
            orderItem.setName(itemReq.getName());
            orderItem.setQty(itemReq.getQty());
            orderItem.setUnitPrice(itemReq.getUnitPrice());
            
            orderItemRepository.save(orderItem);
            log.debug("주문 아이템 생성: 주문ID={}, 상품명={}, 수량={}, 단가={}", 
                order.getId(), itemReq.getName(), itemReq.getQty(), itemReq.getUnitPrice());
        }
        
        // 7. 주문 아이템들과 함께 다시 조회 (LazyLoading 방지)
        return orderRepository.findByOrderNumber(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("생성된 주문을 찾을 수 없습니다."));
    }
    
    /**
     * 주문 제출 - 상태 변경
     */
    @Transactional
    public Order submitOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
        
        // 권한 체크 - 본인 주문만 제출 가능
        if (!order.getUserId().equals(userId)) {
            throw new BusinessRuleViolationException("본인의 주문만 제출할 수 있습니다.");
        }
        
        // 상태 전환 체크
        if (!order.canTransitionTo(Order.OrderStatus.SUBMITTED)) {
            throw new BusinessRuleViolationException(
                String.format("현재 상태(%s)에서는 제출할 수 없습니다.", order.getStatus().getDisplayName()));
        }
        
        order.setStatus(Order.OrderStatus.SUBMITTED);
        order = orderRepository.save(order);
        
        log.info("주문 제출 완료: ID={}, 주문번호={}", order.getId(), order.getOrderNumber());
        return order;
    }
    
    /**
     * 사용자별 주문 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<Order> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * 사용자별 특정 상태 주문 조회
     */
    @Transactional(readOnly = true)
    public List<Order> getUserOrdersByStatus(Long userId, Order.OrderStatus status) {
        // findByUserIdAndStatus 메서드가 Page를 반환하므로 수정
        return orderRepository.findByUserId(userId, PageRequest.of(0, 100))
                .getContent().stream()
                .filter(order -> order.getStatus() == status)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 주문 단건 조회 (사용자 권한 체크)
     */
    @Transactional(readOnly = true)
    public Optional<Order> getOrderByIdAndUserId(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .filter(order -> order.getUserId().equals(userId));
    }
    
    /**
     * 주문번호 생성 - 동시성 안전
     * 형식: YCS-YYYYMMDD-NNN (예: YCS-20240829-001)
     */
    private synchronized String generateOrderNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String orderNoPrefix = "YCS-" + datePrefix + "-";
        
        // 최대 재시도 횟수
        int maxRetries = 10;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // 오늘 생성된 주문 개수 조회
                long todayOrderCount = orderRepository.countTodayOrders();
                
                // 순번 생성 (001부터 시작)
                String sequence = String.format("%03d", todayOrderCount + 1);
                String orderNo = orderNoPrefix + sequence;
                
                // 중복 체크
                if (!orderRepository.existsByOrderNumber(orderNo)) {
                    log.debug("주문번호 생성 성공: {} ({}번째 시도)", orderNo, attempt);
                    return orderNo;
                }
                
                // 중복이면 랜덤하게 약간의 지연 후 재시도
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessRuleViolationException("주문번호 생성 중 인터럽트 발생");
            } catch (Exception e) {
                log.warn("주문번호 생성 시도 실패: {}번째 시도, 오류: {}", attempt, e.getMessage());
            }
        }
        
        // 모든 재시도 실패 시 타임스탬프 기반 폴백
        String fallbackOrderNo = orderNoPrefix + System.currentTimeMillis() % 10000;
        log.warn("주문번호 생성 재시도 한계 초과, 폴백 사용: {}", fallbackOrderNo);
        return fallbackOrderNo;
    }
    
    /**
     * 서버 총액 계산 - 클라이언트 검증용
     */
    private BigDecimal calculateTotalAmount(List<SimpleCreateOrderRequest.OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return items.stream()
                .filter(item -> item.getQty() != null && item.getUnitPrice() != null)
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 주문 삭제 (DRAFT 상태만 가능)
     */
    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다."));
        
        // 권한 체크
        if (!order.getUserId().equals(userId)) {
            throw new BusinessRuleViolationException("본인의 주문만 삭제할 수 있습니다.");
        }
        
        // DRAFT 상태만 삭제 가능
        if (order.getStatus() != Order.OrderStatus.DRAFT) {
            throw new BusinessRuleViolationException("임시저장 상태의 주문만 삭제할 수 있습니다.");
        }
        
        // 연관된 주문 아이템들 먼저 삭제
        orderItemRepository.deleteByOrderId(orderId);
        
        // 주문 삭제
        orderRepository.delete(order);
        
        log.info("주문 삭제 완료: ID={}, 주문번호={}", orderId, order.getOrderNumber());
    }
}