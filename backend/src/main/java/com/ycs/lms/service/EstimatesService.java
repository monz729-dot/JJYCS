package com.ycs.lms.service;

import com.ycs.lms.dto.*;
import com.ycs.lms.entity.Order;
import com.ycs.lms.exception.NotFoundException;
import com.ycs.lms.repository.OrderRepository;
import com.ycs.lms.repository.OrderBoxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EstimatesService {

    private final OrderRepository orderRepository;
    private final OrderBoxRepository orderBoxRepository;
    private final BusinessRuleService businessRuleService;
    
    // Mock storage for estimates (will be replaced with database entity later)
    private final Map<Long, EstimateResponse> estimates = new HashMap<>();
    private Long estimateIdCounter = 1L;

    /**
     * 견적 생성
     */
    public EstimateResponse createEstimate(Long orderId, EstimateCreateRequest request) {
        log.info("Creating estimate for order: {}", orderId);
        
        // Order 조회 (실제 구현에서는 repository 사용)
        Order order = getOrderById(orderId);
        
        // 견적 계산
        EstimateCalculationResponse calculation = calculateEstimate(
            orderId, 
            request.getShippingMethod(), 
            request.getCarrier(), 
            request.getServiceLevel()
        );
        
        Long estimateId = estimateIdCounter++;
        
        EstimateResponse estimate = EstimateResponse.builder()
            .id(estimateId)
            .orderId(orderId)
            .orderCode(order.getOrderCode())
            .version(1) // 1차 견적
            .status("pending")
            .shippingMethod(request.getShippingMethod())
            .carrier(request.getCarrier())
            .serviceLevel(request.getServiceLevel())
            .shippingCost(calculation.getShippingCost())
            .localShippingCost(calculation.getLocalShippingCost())
            .repackingCost(calculation.getRepackingCost())
            .totalCost(calculation.getTotalCost())
            .currency("THB")
            .validUntil(LocalDateTime.now().plusDays(7))
            .notes(request.getNotes())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
            
        estimates.put(estimateId, estimate);
        
        log.info("Created estimate with ID: {}", estimateId);
        return estimate;
    }

    /**
     * 주문별 견적 목록 조회
     */
    public List<EstimateResponse> getEstimates(Long orderId) {
        log.info("Getting estimates for order: {}", orderId);
        
        return estimates.values().stream()
            .filter(estimate -> estimate.getOrderId().equals(orderId))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .toList();
    }

    /**
     * 견적 상세 조회
     */
    public EstimateResponse getEstimate(Long estimateId) {
        log.info("Getting estimate: {}", estimateId);
        
        EstimateResponse estimate = estimates.get(estimateId);
        if (estimate == null) {
            throw new NotFoundException("견적을 찾을 수 없습니다: " + estimateId);
        }
        
        return estimate;
    }

    /**
     * 견적 응답 (승인/거부)
     */
    public EstimateResponse respondToEstimate(Long estimateId, EstimateResponseRequest request) {
        log.info("Responding to estimate {} with action: {}", estimateId, request.getAction());
        
        EstimateResponse estimate = getEstimate(estimateId);
        
        if (!"pending".equals(estimate.getStatus())) {
            throw new IllegalStateException("이미 처리된 견적입니다");
        }
        
        EstimateResponse updatedEstimate = estimate.toBuilder()
            .status(request.getAction()) // "approved" or "rejected"
            .responseNotes(request.getNotes())
            .respondedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
            
        estimates.put(estimateId, updatedEstimate);
        
        return updatedEstimate;
    }

    /**
     * 견적 수정
     */
    public EstimateResponse updateEstimate(Long estimateId, EstimateCreateRequest request) {
        log.info("Updating estimate: {}", estimateId);
        
        EstimateResponse existingEstimate = getEstimate(estimateId);
        
        // 견적 재계산
        EstimateCalculationResponse calculation = calculateEstimate(
            existingEstimate.getOrderId(), 
            request.getShippingMethod(), 
            request.getCarrier(), 
            request.getServiceLevel()
        );
        
        EstimateResponse updatedEstimate = existingEstimate.toBuilder()
            .shippingMethod(request.getShippingMethod())
            .carrier(request.getCarrier())
            .serviceLevel(request.getServiceLevel())
            .shippingCost(calculation.getShippingCost())
            .localShippingCost(calculation.getLocalShippingCost())
            .repackingCost(calculation.getRepackingCost())
            .totalCost(calculation.getTotalCost())
            .notes(request.getNotes())
            .updatedAt(LocalDateTime.now())
            .build();
            
        estimates.put(estimateId, updatedEstimate);
        
        return updatedEstimate;
    }

    /**
     * 2차 견적 생성
     */
    public EstimateResponse createSecondEstimate(Long firstEstimateId, EstimateCreateRequest request) {
        log.info("Creating second estimate based on: {}", firstEstimateId);
        
        EstimateResponse firstEstimate = getEstimate(firstEstimateId);
        
        // 견적 계산
        EstimateCalculationResponse calculation = calculateEstimate(
            firstEstimate.getOrderId(), 
            request.getShippingMethod(), 
            request.getCarrier(), 
            request.getServiceLevel()
        );
        
        Long estimateId = estimateIdCounter++;
        
        EstimateResponse secondEstimate = EstimateResponse.builder()
            .id(estimateId)
            .orderId(firstEstimate.getOrderId())
            .orderCode(firstEstimate.getOrderCode())
            .version(2) // 2차 견적
            .status("pending")
            .shippingMethod(request.getShippingMethod())
            .carrier(request.getCarrier())
            .serviceLevel(request.getServiceLevel())
            .shippingCost(calculation.getShippingCost())
            .localShippingCost(calculation.getLocalShippingCost())
            .repackingCost(calculation.getRepackingCost())
            .totalCost(calculation.getTotalCost())
            .currency("THB")
            .validUntil(LocalDateTime.now().plusDays(7))
            .notes(request.getNotes())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
            
        estimates.put(estimateId, secondEstimate);
        
        return secondEstimate;
    }

    /**
     * 견적 자동 계산
     */
    public EstimateCalculationResponse calculateEstimate(Long orderId, String shippingMethod, 
                                                        String carrier, String serviceLevel) {
        log.info("Calculating estimate for order: {}, method: {}", orderId, shippingMethod);
        
        Order order = getOrderById(orderId);
        
        // CBM 계산 (Repository에서 직접 조회)
        BigDecimal totalCbm = orderBoxRepository.sumCbmByOrderId(orderId);
        if (totalCbm == null) totalCbm = BigDecimal.ZERO;
        
        // 무게 계산 (Repository에서 직접 조회)
        BigDecimal totalWeight = orderBoxRepository.sumWeightByOrderId(orderId);
        if (totalWeight == null) totalWeight = BigDecimal.ZERO;
        
        // 배송비 계산 (Mock)
        BigDecimal shippingCost = calculateShippingCost(totalCbm, totalWeight, shippingMethod, carrier);
        
        // 로컬 배송비 (Mock)
        BigDecimal localShippingCost = BigDecimal.valueOf(150); // THB
        
        // 리패킹 비용 (필요시)
        BigDecimal repackingCost = order.isRequiresRepacking() ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        
        // 총 비용
        BigDecimal totalCost = shippingCost.add(localShippingCost).add(repackingCost);
        
        return EstimateCalculationResponse.builder()
            .orderId(orderId)
            .totalCbm(totalCbm)
            .totalWeight(totalWeight)
            .shippingMethod(shippingMethod)
            .carrier(carrier)
            .serviceLevel(serviceLevel)
            .shippingCost(shippingCost)
            .localShippingCost(localShippingCost)
            .repackingCost(repackingCost)
            .totalCost(totalCost)
            .currency("THB")
            .build();
    }

    /**
     * 견적 취소
     */
    public void cancelEstimate(Long estimateId) {
        log.info("Cancelling estimate: {}", estimateId);
        
        EstimateResponse estimate = getEstimate(estimateId);
        
        EstimateResponse cancelledEstimate = estimate.toBuilder()
            .status("cancelled")
            .updatedAt(LocalDateTime.now())
            .build();
            
        estimates.put(estimateId, cancelledEstimate);
    }

    /**
     * 배송비 계산 (Mock 구현)
     */
    private BigDecimal calculateShippingCost(BigDecimal cbm, BigDecimal weight, String method, String carrier) {
        BigDecimal baseCost = BigDecimal.valueOf(500); // 기본 배송비 THB
        
        if ("air".equals(method)) {
            baseCost = baseCost.multiply(BigDecimal.valueOf(1.5)); // 항공 할증
        }
        
        // CBM당 요금
        BigDecimal cbmCost = cbm.multiply(BigDecimal.valueOf(200)); // THB per CBM
        
        // 무게당 요금
        BigDecimal weightCost = weight.multiply(BigDecimal.valueOf(10)); // THB per KG
        
        return baseCost.add(cbmCost).add(weightCost);
    }

    /**
     * Order 조회 (JPA Repository 사용)
     */
    private Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다: " + orderId));
    }
}