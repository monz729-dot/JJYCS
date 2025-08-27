package com.ycs.lms.service;

import com.ycs.lms.entity.*;
import com.ycs.lms.repository.InvoiceRepository;
import com.ycs.lms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BillingService {
    
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    
    @Value("${billing.vat-rate:0.10}")
    private BigDecimal vatRate;
    
    @Value("${billing.base-shipping-rate:15000}")
    private BigDecimal baseShippingRate;
    
    @Value("${billing.weight-rate-per-kg:2000}")
    private BigDecimal weightRatePerKg;
    
    @Value("${billing.cbm-rate:50000}")
    private BigDecimal cbmRate;
    
    @Value("${billing.repacking-rate:5000}")
    private BigDecimal repackingRate;
    
    @Value("${billing.airfreight-surcharge-rate:25000}")
    private BigDecimal airfreightSurchargeRate;
    
    @Value("${billing.local-delivery-rate:8000}")
    private BigDecimal localDeliveryRate;
    
    @Value("${billing.thb-exchange-rate:40.0}")
    private BigDecimal defaultThbExchangeRate;
    
    /**
     * Proforma Invoice (1차 견적서) 생성
     */
    public Invoice generateProformaInvoice(Long orderId, String createdBy) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        // 기존 Proforma Invoice가 있는지 확인
        Optional<Invoice> existingInvoice = invoiceRepository
            .findByOrderIdAndInvoiceType(orderId, Invoice.InvoiceType.PROFORMA);
        
        if (existingInvoice.isPresent()) {
            throw new IllegalArgumentException("Proforma invoice already exists for this order");
        }
        
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber(Invoice.InvoiceType.PROFORMA));
        invoice.setOrder(order);
        invoice.setUser(order.getUser());
        invoice.setInvoiceType(Invoice.InvoiceType.PROFORMA);
        invoice.setStatus(Invoice.InvoiceStatus.DRAFT);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setCreatedBy(createdBy);
        invoice.setCurrency("KRW");
        invoice.setExchangeRate(defaultThbExchangeRate);
        
        // 기본 배송료 계산
        calculateShippingCosts(invoice, order);
        
        // 총액 계산
        calculateTotalAmount(invoice);
        
        return invoiceRepository.save(invoice);
    }
    
    /**
     * 기본 배송료 계산 (예상 무게/부피 기준)
     */
    private void calculateShippingCosts(Invoice invoice, Order order) {
        // 기본 배송료
        invoice.setBaseShippingFee(baseShippingRate);
        
        // 무게 기반 추가료
        if (order.getTotalWeight() != null && order.getTotalWeight().compareTo(BigDecimal.valueOf(5)) > 0) {
            BigDecimal excessWeight = order.getTotalWeight().subtract(BigDecimal.valueOf(5));
            invoice.setWeightSurcharge(excessWeight.multiply(weightRatePerKg));
        }
        
        // CBM 기반 추가료
        if (order.getTotalCbm() != null && order.getTotalCbm().compareTo(BigDecimal.valueOf(0.1)) > 0) {
            BigDecimal excessCbm = order.getTotalCbm().subtract(BigDecimal.valueOf(0.1));
            invoice.setVolumeSurcharge(excessCbm.multiply(cbmRate));
        }
        
        // 리패킹 비용
        if (order.getRepacking() != null && order.getRepacking()) {
            invoice.setRepackingFee(repackingRate);
        }
        
        // CBM 초과시 항공료 추가비 (29 CBM 초과)
        if (order.getTotalCbm() != null && order.getTotalCbm().compareTo(BigDecimal.valueOf(29)) > 0) {
            invoice.setAirfreightSurcharge(airfreightSurchargeRate);
        }
        
        // 로컬 배송료
        if (order.getLocalDelivery() != null && order.getLocalDelivery()) {
            invoice.setLocalDeliveryFee(localDeliveryRate);
        }
    }
    
    /**
     * 총액 계산
     */
    private void calculateTotalAmount(Invoice invoice) {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        subtotal = subtotal.add(invoice.getBaseShippingFee() != null ? invoice.getBaseShippingFee() : BigDecimal.ZERO);
        subtotal = subtotal.add(invoice.getWeightSurcharge() != null ? invoice.getWeightSurcharge() : BigDecimal.ZERO);
        subtotal = subtotal.add(invoice.getVolumeSurcharge() != null ? invoice.getVolumeSurcharge() : BigDecimal.ZERO);
        subtotal = subtotal.add(invoice.getRepackingFee() != null ? invoice.getRepackingFee() : BigDecimal.ZERO);
        subtotal = subtotal.add(invoice.getAirfreightSurcharge() != null ? invoice.getAirfreightSurcharge() : BigDecimal.ZERO);
        subtotal = subtotal.add(invoice.getLocalDeliveryFee() != null ? invoice.getLocalDeliveryFee() : BigDecimal.ZERO);
        subtotal = subtotal.add(invoice.getMiscellaneousFee() != null ? invoice.getMiscellaneousFee() : BigDecimal.ZERO);
        subtotal = subtotal.subtract(invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO);
        
        // VAT 계산
        BigDecimal vat = subtotal.multiply(vatRate).setScale(0, RoundingMode.HALF_UP);
        invoice.setVatAmount(vat);
        
        // 총액
        BigDecimal total = subtotal.add(vat);
        invoice.setTotalAmount(total);
        
        // THB 환산
        if (invoice.getExchangeRate() != null) {
            BigDecimal thbAmount = total.divide(invoice.getExchangeRate(), 2, RoundingMode.HALF_UP);
            invoice.setThbAmount(thbAmount);
        }
    }
    
    /**
     * 청구서 번호 생성
     */
    private String generateInvoiceNumber(Invoice.InvoiceType type) {
        String prefix = switch (type) {
            case PROFORMA -> "PF";
            case ADDITIONAL -> "AD";
            case FINAL -> "FI";
        };
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = System.currentTimeMillis() % 10000;
        
        return String.format("YCS-%s-%s-%04d", prefix, timestamp, sequence);
    }
    
    /**
     * 청구서 발행
     */
    public Invoice issueInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        
        if (invoice.getStatus() != Invoice.InvoiceStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft invoices can be issued");
        }
        
        invoice.setStatus(Invoice.InvoiceStatus.ISSUED);
        invoice.setIssuedAt(LocalDateTime.now());
        invoice.setDueDate(LocalDateTime.now().plusDays(7)); // 7일 결제 기한
        invoice.setUpdatedAt(LocalDateTime.now());
        
        return invoiceRepository.save(invoice);
    }
    
    /**
     * 결제 처리
     */
    public Invoice processPayment(Long invoiceId, BigDecimal paidAmount) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        
        BigDecimal currentPaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newPaidAmount = currentPaid.add(paidAmount);
        
        invoice.setPaidAmount(newPaidAmount);
        invoice.setUpdatedAt(LocalDateTime.now());
        
        // 상태 업데이트
        if (newPaidAmount.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(Invoice.InvoiceStatus.FULLY_PAID);
            invoice.setPaidAt(LocalDateTime.now());
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
        }
        
        return invoiceRepository.save(invoice);
    }
}