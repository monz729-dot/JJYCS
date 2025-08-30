package com.ysc.lms.service;

import com.ysc.lms.entity.Billing;
import com.ysc.lms.entity.Order;
import com.ysc.lms.repository.BillingRepository;
import com.ysc.lms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * P0-3: 청구/결제 로직 서비스
 * - 선청구 기능 (배송 완료 전 청구 가능)
 * - 청구일 기준 네이버 환율 적용
 * - 이중 통화 표시 (THB/KRW)
 * - 다중 결제 수단 지원
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {
    
    private final BillingRepository billingRepository;
    private final OrderRepository orderRepository;
    private final ExchangeRateService exchangeRateService;
    
    /**
     * 청구서 생성 (선청구 지원)
     */
    @Transactional
    public Billing createBilling(Long orderId, BillingRequest request) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
            
            // 이미 청구서가 있는지 확인
            Optional<Billing> existingBilling = billingRepository.findByOrderId(orderId);
            if (existingBilling.isPresent()) {
                throw new IllegalStateException("이미 청구서가 생성된 주문입니다: " + orderId);
            }
            
            log.info("Creating billing for order: {} (Pre-billing enabled)", orderId);
            
            // 청구서 생성
            Billing billing = new Billing();
            billing.setOrder(order);
            
            // 비용 항목 설정 (THB)
            billing.setShippingFee(request.getShippingFee());
            billing.setLocalDeliveryFee(request.getLocalDeliveryFee());
            billing.setRepackingFee(request.getRepackingFee());
            billing.setHandlingFee(request.getHandlingFee());
            billing.setInsuranceFee(request.getInsuranceFee());
            billing.setCustomsFee(request.getCustomsFee());
            
            // 청구일 기준 환율 적용
            LocalDateTime billingDate = LocalDateTime.now();
            ExchangeRateService.BillingExchangeRate exchangeRateInfo = 
                exchangeRateService.getThbToKrwRateForBilling(billingDate);
            
            if (exchangeRateInfo.isValid()) {
                billing.updateExchangeRate(
                    exchangeRateInfo.getRate(), 
                    exchangeRateInfo.getRateDate(), 
                    exchangeRateInfo.getSource()
                );
                log.info("Applied exchange rate {} from {} for billing", 
                        exchangeRateInfo.getRate(), exchangeRateInfo.getSource());
            } else {
                log.warn("Invalid exchange rate, billing created without KRW amounts: {}", 
                        exchangeRateInfo.getMessage());
            }
            
            // Proforma 청구서 발행
            billing.setProformaIssued(true);
            billing.setProformaDate(billingDate);
            
            return billingRepository.save(billing);
            
        } catch (Exception e) {
            log.error("Error creating billing for order: {}", orderId, e);
            throw new RuntimeException("청구서 생성 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 청구서 미리보기 (결제 전 고객 확인용)
     */
    public BillingPreview generateBillingPreview(Long orderId, BillingRequest request) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
            
            log.info("Generating billing preview for order: {}", orderId);
            
            // THB 금액 계산
            BigDecimal subtotalThb = request.getShippingFee()
                .add(request.getLocalDeliveryFee())
                .add(request.getRepackingFee())
                .add(request.getHandlingFee())
                .add(request.getInsuranceFee())
                .add(request.getCustomsFee());
            
            BigDecimal taxThb = subtotalThb.multiply(new BigDecimal("0.07"))
                                         .setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalThb = subtotalThb.add(taxThb);
            
            // 환율 조회 및 KRW 금액 계산
            ExchangeRateService.BillingExchangeRate exchangeRateInfo = 
                exchangeRateService.getThbToKrwRateForBilling(LocalDateTime.now());
            
            BillingPreview preview = new BillingPreview();
            preview.setOrderId(orderId);
            preview.setOrderNumber(order.getOrderNumber());
            
            // THB 금액
            preview.setSubtotalThb(subtotalThb);
            preview.setTaxThb(taxThb);
            preview.setTotalThb(totalThb);
            
            // KRW 금액 (환율 적용)
            if (exchangeRateInfo.isValid()) {
                BigDecimal rate = exchangeRateInfo.getRate();
                preview.setExchangeRate(rate);
                preview.setExchangeRateSource(exchangeRateInfo.getSource());
                preview.setExchangeRateDate(exchangeRateInfo.getRateDate());
                
                preview.setSubtotalKrw(exchangeRateService.convertThbToKrwForBilling(subtotalThb, rate));
                preview.setTaxKrw(exchangeRateService.convertThbToKrwForBilling(taxThb, rate));
                preview.setTotalKrw(exchangeRateService.convertThbToKrwForBilling(totalThb, rate));
            } else {
                log.warn("Cannot convert to KRW for preview: {}", exchangeRateInfo.getMessage());
                preview.setExchangeRateError(exchangeRateInfo.getMessage());
            }
            
            // 사용 가능한 결제 수단 설정
            preview.setAvailablePaymentMethods(getAvailablePaymentMethods(order));
            
            return preview;
            
        } catch (Exception e) {
            log.error("Error generating billing preview for order: {}", orderId, e);
            throw new RuntimeException("청구서 미리보기 생성 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 최종 청구서 발행 (결제 확인 후)
     */
    @Transactional
    public Billing issueFinalBilling(Long billingId) {
        try {
            Billing billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new IllegalArgumentException("청구서를 찾을 수 없습니다: " + billingId));
            
            if (billing.getFinalIssued()) {
                log.warn("Final billing already issued for billing: {}", billingId);
                return billing;
            }
            
            // 최종 청구서 발행
            billing.setFinalIssued(true);
            billing.setFinalDate(LocalDateTime.now());
            
            log.info("Final billing issued for billing: {}", billingId);
            return billingRepository.save(billing);
            
        } catch (Exception e) {
            log.error("Error issuing final billing: {}", billingId, e);
            throw new RuntimeException("최종 청구서 발행 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 결제 확인 처리
     */
    @Transactional
    public Billing confirmPayment(Long billingId, PaymentConfirmRequest request) {
        try {
            Billing billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new IllegalArgumentException("청구서를 찾을 수 없습니다: " + billingId));
            
            // 결제 정보 업데이트
            billing.setPaymentStatus(Billing.PaymentStatus.CONFIRMED);
            billing.setPaymentDate(request.getPaymentDate());
            billing.setPaymentReference(request.getPaymentReference());
            billing.setDepositorName(request.getDepositorName());
            billing.setPaymentMethod(request.getPaymentMethod());
            
            log.info("Payment confirmed for billing: {} with method: {}", 
                    billingId, request.getPaymentMethod());
            
            return billingRepository.save(billing);
            
        } catch (Exception e) {
            log.error("Error confirming payment for billing: {}", billingId, e);
            throw new RuntimeException("결제 확인 처리 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 사용자별 미결제 청구서 조회
     */
    public List<Billing> getPendingBillings(Long userId) {
        try {
            return billingRepository.findPendingBillingsByUserId(userId);
        } catch (Exception e) {
            log.error("Error getting pending billings for user: {}", userId, e);
            throw new RuntimeException("미결제 청구서 조회 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 주문별 사용 가능한 결제 수단 조회
     */
    private List<Billing.PaymentMethod> getAvailablePaymentMethods(Order order) {
        // 사용자 국가/지역에 따른 결제 수단 필터링
        String userCountry = getUserCountry(order);
        
        if ("KOREA".equalsIgnoreCase(userCountry)) {
            return List.of(
                Billing.PaymentMethod.KR_BANK_TRANSFER,
                Billing.PaymentMethod.KR_CREDIT_CARD,
                Billing.PaymentMethod.KR_PG_KAKAOPAY,
                Billing.PaymentMethod.KR_PG_NAVERPAY
            );
        } else if ("THAILAND".equalsIgnoreCase(userCountry)) {
            return List.of(
                Billing.PaymentMethod.TH_QR_PROMPTPAY,
                Billing.PaymentMethod.TH_BANK_TRANSFER,
                Billing.PaymentMethod.TH_CREDIT_CARD,
                Billing.PaymentMethod.TH_TRUE_MONEY
            );
        } else {
            return List.of(
                Billing.PaymentMethod.PAYPAL,
                Billing.PaymentMethod.BANK_TRANSFER
            );
        }
    }
    
    private String getUserCountry(Order order) {
        // 주문의 배송 주소 또는 사용자 정보에서 국가 추출
        if (order.getUser().getAddress() != null) {
            String address = order.getUser().getAddress().toLowerCase();
            if (address.contains("korea") || address.contains("한국")) {
                return "KOREA";
            } else if (address.contains("thailand") || address.contains("태국")) {
                return "THAILAND";
            }
        }
        return "OTHER";
    }
    
    // DTO 클래스들
    
    public static class BillingRequest {
        private BigDecimal shippingFee = BigDecimal.ZERO;
        private BigDecimal localDeliveryFee = BigDecimal.ZERO;
        private BigDecimal repackingFee = BigDecimal.ZERO;
        private BigDecimal handlingFee = BigDecimal.ZERO;
        private BigDecimal insuranceFee = BigDecimal.ZERO;
        private BigDecimal customsFee = BigDecimal.ZERO;
        
        // Getters and Setters
        public BigDecimal getShippingFee() { return shippingFee; }
        public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
        
        public BigDecimal getLocalDeliveryFee() { return localDeliveryFee; }
        public void setLocalDeliveryFee(BigDecimal localDeliveryFee) { this.localDeliveryFee = localDeliveryFee; }
        
        public BigDecimal getRepackingFee() { return repackingFee; }
        public void setRepackingFee(BigDecimal repackingFee) { this.repackingFee = repackingFee; }
        
        public BigDecimal getHandlingFee() { return handlingFee; }
        public void setHandlingFee(BigDecimal handlingFee) { this.handlingFee = handlingFee; }
        
        public BigDecimal getInsuranceFee() { return insuranceFee; }
        public void setInsuranceFee(BigDecimal insuranceFee) { this.insuranceFee = insuranceFee; }
        
        public BigDecimal getCustomsFee() { return customsFee; }
        public void setCustomsFee(BigDecimal customsFee) { this.customsFee = customsFee; }
    }
    
    public static class BillingPreview {
        private Long orderId;
        private String orderNumber;
        private BigDecimal subtotalThb;
        private BigDecimal taxThb;
        private BigDecimal totalThb;
        private BigDecimal subtotalKrw;
        private BigDecimal taxKrw;
        private BigDecimal totalKrw;
        private BigDecimal exchangeRate;
        private String exchangeRateSource;
        private LocalDateTime exchangeRateDate;
        private String exchangeRateError;
        private List<Billing.PaymentMethod> availablePaymentMethods;
        
        // Getters and Setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        
        public BigDecimal getSubtotalThb() { return subtotalThb; }
        public void setSubtotalThb(BigDecimal subtotalThb) { this.subtotalThb = subtotalThb; }
        
        public BigDecimal getTaxThb() { return taxThb; }
        public void setTaxThb(BigDecimal taxThb) { this.taxThb = taxThb; }
        
        public BigDecimal getTotalThb() { return totalThb; }
        public void setTotalThb(BigDecimal totalThb) { this.totalThb = totalThb; }
        
        public BigDecimal getSubtotalKrw() { return subtotalKrw; }
        public void setSubtotalKrw(BigDecimal subtotalKrw) { this.subtotalKrw = subtotalKrw; }
        
        public BigDecimal getTaxKrw() { return taxKrw; }
        public void setTaxKrw(BigDecimal taxKrw) { this.taxKrw = taxKrw; }
        
        public BigDecimal getTotalKrw() { return totalKrw; }
        public void setTotalKrw(BigDecimal totalKrw) { this.totalKrw = totalKrw; }
        
        public BigDecimal getExchangeRate() { return exchangeRate; }
        public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
        
        public String getExchangeRateSource() { return exchangeRateSource; }
        public void setExchangeRateSource(String exchangeRateSource) { this.exchangeRateSource = exchangeRateSource; }
        
        public LocalDateTime getExchangeRateDate() { return exchangeRateDate; }
        public void setExchangeRateDate(LocalDateTime exchangeRateDate) { this.exchangeRateDate = exchangeRateDate; }
        
        public String getExchangeRateError() { return exchangeRateError; }
        public void setExchangeRateError(String exchangeRateError) { this.exchangeRateError = exchangeRateError; }
        
        public List<Billing.PaymentMethod> getAvailablePaymentMethods() { return availablePaymentMethods; }
        public void setAvailablePaymentMethods(List<Billing.PaymentMethod> availablePaymentMethods) { 
            this.availablePaymentMethods = availablePaymentMethods; 
        }
    }
    
    public static class PaymentConfirmRequest {
        private LocalDateTime paymentDate;
        private String paymentReference;
        private String depositorName;
        private Billing.PaymentMethod paymentMethod;
        
        // Getters and Setters
        public LocalDateTime getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
        
        public String getPaymentReference() { return paymentReference; }
        public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
        
        public String getDepositorName() { return depositorName; }
        public void setDepositorName(String depositorName) { this.depositorName = depositorName; }
        
        public Billing.PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(Billing.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    }
}