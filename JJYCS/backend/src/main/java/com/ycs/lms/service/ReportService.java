package com.ycs.lms.service;

import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.Invoice;
import com.ycs.lms.repository.OrderRepository;
import com.ycs.lms.repository.InvoiceRepository;
import com.ycs.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportService {
    
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    
    /**
     * 대시보드용 종합 통계
     */
    public DashboardStatistics getDashboardStatistics() {
        DashboardStatistics stats = new DashboardStatistics();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime yearStart = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        
        // 주문 통계
        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus(Order.OrderStatus.RECEIVED));
        stats.setProcessingOrders(orderRepository.countByStatus(Order.OrderStatus.PROCESSING));
        stats.setDeliveredOrders(orderRepository.countByStatus(Order.OrderStatus.DELIVERED));
        stats.setTodayOrders(orderRepository.countOrdersByDateRange(now.toLocalDate().atStartOfDay(), now));
        stats.setMonthlyOrders(orderRepository.countOrdersByDateRange(monthStart, now));
        
        // CBM 관련 통계
        List<Order> ordersWithCbm = orderRepository.findAll();
        long cbmExceededOrders = ordersWithCbm.stream()
            .filter(order -> order.getTotalCbm() != null && order.getTotalCbm().compareTo(BigDecimal.valueOf(29)) > 0)
            .count();
        stats.setCbmExceededOrders(cbmExceededOrders);
        
        // THB 임계값 초과 주문 수
        long thbExceededOrders = ordersWithCbm.stream()
            .filter(order -> order.getRequiresExtraRecipient() != null && order.getRequiresExtraRecipient())
            .count();
        stats.setThbExceededOrders(thbExceededOrders);
        
        // 회원 코드 미기재 주문 수
        long noMemberCodeOrders = ordersWithCbm.stream()
            .filter(order -> order.getHasNoMemberCode() != null && order.getHasNoMemberCode())
            .count();
        stats.setNoMemberCodeOrders(noMemberCodeOrders);
        
        // 매출 통계
        BigDecimal monthlyRevenue = invoiceRepository.getTotalPaidAmountBetweenDates(monthStart, now);
        stats.setMonthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
        
        BigDecimal yearlyRevenue = invoiceRepository.getTotalPaidAmountBetweenDates(yearStart, now);
        stats.setYearlyRevenue(yearlyRevenue != null ? yearlyRevenue : BigDecimal.ZERO);
        
        // 청구서 통계
        stats.setPendingInvoices(invoiceRepository.countByStatus(Invoice.InvoiceStatus.PAYMENT_PENDING));
        stats.setOverdueInvoices(invoiceRepository.countByStatus(Invoice.InvoiceStatus.OVERDUE));
        
        log.info("Generated dashboard statistics: {} total orders, {} pending, {} monthly revenue", 
            stats.getTotalOrders(), stats.getPendingOrders(), stats.getMonthlyRevenue());
        
        return stats;
    }
    
    /**
     * 월별 매출 리포트
     */
    public List<MonthlyRevenueReport> getMonthlyRevenueReport(int year) {
        List<Object[]> rawData = invoiceRepository.getMonthlyRevenue(year);
        
        Map<Integer, BigDecimal> revenueByMonth = rawData.stream()
            .collect(Collectors.toMap(
                row -> (Integer) row[0],
                row -> (BigDecimal) row[1],
                (existing, replacement) -> existing
            ));
        
        List<MonthlyRevenueReport> report = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            MonthlyRevenueReport monthReport = new MonthlyRevenueReport();
            monthReport.setYear(year);
            monthReport.setMonth(month);
            monthReport.setRevenue(revenueByMonth.getOrDefault(month, BigDecimal.ZERO));
            
            // 해당 월의 주문 수
            LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            monthReport.setOrderCount(orderRepository.countOrdersByDateRange(monthStart, monthEnd));
            
            report.add(monthReport);
        }
        
        return report;
    }
    
    /**
     * CBM 분석 리포트
     */
    public CbmAnalysisReport getCbmAnalysisReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        List<Order> orders = orderRepository.findOrdersByDateRange(start, end);
        
        CbmAnalysisReport report = new CbmAnalysisReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalOrders(orders.size());
        
        // CBM 통계
        List<BigDecimal> cbmValues = orders.stream()
            .map(Order::getTotalCbm)
            .filter(Objects::nonNull)
            .sorted()
            .collect(Collectors.toList());
        
        if (!cbmValues.isEmpty()) {
            report.setMinCbm(cbmValues.get(0));
            report.setMaxCbm(cbmValues.get(cbmValues.size() - 1));
            
            BigDecimal avgCbm = cbmValues.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(cbmValues.size()), 6, RoundingMode.HALF_UP);
            report.setAverageCbm(avgCbm);
        }
        
        // CBM 임계값 초과 분석
        long exceededCount = orders.stream()
            .filter(order -> order.getTotalCbm() != null && 
                           order.getTotalCbm().compareTo(BigDecimal.valueOf(29)) > 0)
            .count();
        report.setCbmExceededCount(exceededCount);
        report.setCbmExceededPercentage(
            orders.isEmpty() ? BigDecimal.ZERO : 
            BigDecimal.valueOf(exceededCount * 100.0 / orders.size()).setScale(2, RoundingMode.HALF_UP)
        );
        
        // 배송 방식 분포
        Map<String, Long> shippingTypeCounts = orders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getOrderType() != null ? order.getOrderType().name() : "UNKNOWN",
                Collectors.counting()
            ));
        report.setShippingTypeDistribution(shippingTypeCounts);
        
        return report;
    }
    
    /**
     * 비즈니스 룰 위반 리포트
     */
    public BusinessRuleViolationReport getBusinessRuleViolationReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        
        List<Order> orders = orderRepository.findOrdersByDateRange(start, end);
        
        BusinessRuleViolationReport report = new BusinessRuleViolationReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalOrders(orders.size());
        
        // CBM 초과 주문들
        List<Order> cbmExceededOrders = orders.stream()
            .filter(order -> order.getTotalCbm() != null && 
                           order.getTotalCbm().compareTo(BigDecimal.valueOf(29)) > 0)
            .collect(Collectors.toList());
        report.setCbmViolationCount(cbmExceededOrders.size());
        
        // THB 임계값 초과 주문들
        List<Order> thbExceededOrders = orders.stream()
            .filter(order -> order.getRequiresExtraRecipient() != null && 
                           order.getRequiresExtraRecipient())
            .collect(Collectors.toList());
        report.setThbViolationCount(thbExceededOrders.size());
        
        // 회원 코드 미기재 주문들
        List<Order> noMemberCodeOrders = orders.stream()
            .filter(order -> order.getHasNoMemberCode() != null && 
                           order.getHasNoMemberCode())
            .collect(Collectors.toList());
        report.setNoMemberCodeCount(noMemberCodeOrders.size());
        
        // 위반 상세 정보
        List<BusinessRuleViolationDetail> violations = new ArrayList<>();
        
        // CBM 위반 상세
        cbmExceededOrders.forEach(order -> {
            BusinessRuleViolationDetail detail = new BusinessRuleViolationDetail();
            detail.setOrderNumber(order.getOrderNumber());
            detail.setViolationType("CBM_EXCEEDED");
            detail.setDescription("CBM " + order.getTotalCbm() + " > 29.0");
            detail.setCreatedAt(order.getCreatedAt());
            violations.add(detail);
        });
        
        // THB 위반 상세
        thbExceededOrders.forEach(order -> {
            BusinessRuleViolationDetail detail = new BusinessRuleViolationDetail();
            detail.setOrderNumber(order.getOrderNumber());
            detail.setViolationType("THB_EXCEEDED");
            detail.setDescription("THB value > 1,500");
            detail.setCreatedAt(order.getCreatedAt());
            violations.add(detail);
        });
        
        // 회원 코드 미기재 상세
        noMemberCodeOrders.forEach(order -> {
            BusinessRuleViolationDetail detail = new BusinessRuleViolationDetail();
            detail.setOrderNumber(order.getOrderNumber());
            detail.setViolationType("NO_MEMBER_CODE");
            detail.setDescription("Member code not provided");
            detail.setCreatedAt(order.getCreatedAt());
            violations.add(detail);
        });
        
        report.setViolationDetails(violations);
        
        return report;
    }
    
    // DTO 클래스들
    public static class DashboardStatistics {
        private Long totalOrders = 0L;
        private Long pendingOrders = 0L;
        private Long processingOrders = 0L;
        private Long deliveredOrders = 0L;
        private Long todayOrders = 0L;
        private Long monthlyOrders = 0L;
        private Long cbmExceededOrders = 0L;
        private Long thbExceededOrders = 0L;
        private Long noMemberCodeOrders = 0L;
        private BigDecimal monthlyRevenue = BigDecimal.ZERO;
        private BigDecimal yearlyRevenue = BigDecimal.ZERO;
        private Long pendingInvoices = 0L;
        private Long overdueInvoices = 0L;
        
        // Getters and Setters
        public Long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
        public Long getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(Long pendingOrders) { this.pendingOrders = pendingOrders; }
        public Long getProcessingOrders() { return processingOrders; }
        public void setProcessingOrders(Long processingOrders) { this.processingOrders = processingOrders; }
        public Long getDeliveredOrders() { return deliveredOrders; }
        public void setDeliveredOrders(Long deliveredOrders) { this.deliveredOrders = deliveredOrders; }
        public Long getTodayOrders() { return todayOrders; }
        public void setTodayOrders(Long todayOrders) { this.todayOrders = todayOrders; }
        public Long getMonthlyOrders() { return monthlyOrders; }
        public void setMonthlyOrders(Long monthlyOrders) { this.monthlyOrders = monthlyOrders; }
        public Long getCbmExceededOrders() { return cbmExceededOrders; }
        public void setCbmExceededOrders(Long cbmExceededOrders) { this.cbmExceededOrders = cbmExceededOrders; }
        public Long getThbExceededOrders() { return thbExceededOrders; }
        public void setThbExceededOrders(Long thbExceededOrders) { this.thbExceededOrders = thbExceededOrders; }
        public Long getNoMemberCodeOrders() { return noMemberCodeOrders; }
        public void setNoMemberCodeOrders(Long noMemberCodeOrders) { this.noMemberCodeOrders = noMemberCodeOrders; }
        public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
        public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
        public BigDecimal getYearlyRevenue() { return yearlyRevenue; }
        public void setYearlyRevenue(BigDecimal yearlyRevenue) { this.yearlyRevenue = yearlyRevenue; }
        public Long getPendingInvoices() { return pendingInvoices; }
        public void setPendingInvoices(Long pendingInvoices) { this.pendingInvoices = pendingInvoices; }
        public Long getOverdueInvoices() { return overdueInvoices; }
        public void setOverdueInvoices(Long overdueInvoices) { this.overdueInvoices = overdueInvoices; }
    }
    
    public static class MonthlyRevenueReport {
        private Integer year;
        private Integer month;
        private BigDecimal revenue = BigDecimal.ZERO;
        private Long orderCount = 0L;
        
        // Getters and Setters
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
        public Long getOrderCount() { return orderCount; }
        public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }
    }
    
    public static class CbmAnalysisReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalOrders = 0;
        private BigDecimal minCbm = BigDecimal.ZERO;
        private BigDecimal maxCbm = BigDecimal.ZERO;
        private BigDecimal averageCbm = BigDecimal.ZERO;
        private Long cbmExceededCount = 0L;
        private BigDecimal cbmExceededPercentage = BigDecimal.ZERO;
        private Map<String, Long> shippingTypeDistribution = new HashMap<>();
        
        // Getters and Setters
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public Integer getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
        public BigDecimal getMinCbm() { return minCbm; }
        public void setMinCbm(BigDecimal minCbm) { this.minCbm = minCbm; }
        public BigDecimal getMaxCbm() { return maxCbm; }
        public void setMaxCbm(BigDecimal maxCbm) { this.maxCbm = maxCbm; }
        public BigDecimal getAverageCbm() { return averageCbm; }
        public void setAverageCbm(BigDecimal averageCbm) { this.averageCbm = averageCbm; }
        public Long getCbmExceededCount() { return cbmExceededCount; }
        public void setCbmExceededCount(Long cbmExceededCount) { this.cbmExceededCount = cbmExceededCount; }
        public BigDecimal getCbmExceededPercentage() { return cbmExceededPercentage; }
        public void setCbmExceededPercentage(BigDecimal cbmExceededPercentage) { this.cbmExceededPercentage = cbmExceededPercentage; }
        public Map<String, Long> getShippingTypeDistribution() { return shippingTypeDistribution; }
        public void setShippingTypeDistribution(Map<String, Long> shippingTypeDistribution) { this.shippingTypeDistribution = shippingTypeDistribution; }
    }
    
    public static class BusinessRuleViolationReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalOrders = 0;
        private Integer cbmViolationCount = 0;
        private Integer thbViolationCount = 0;
        private Integer noMemberCodeCount = 0;
        private List<BusinessRuleViolationDetail> violationDetails = new ArrayList<>();
        
        // Getters and Setters
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public Integer getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
        public Integer getCbmViolationCount() { return cbmViolationCount; }
        public void setCbmViolationCount(Integer cbmViolationCount) { this.cbmViolationCount = cbmViolationCount; }
        public Integer getThbViolationCount() { return thbViolationCount; }
        public void setThbViolationCount(Integer thbViolationCount) { this.thbViolationCount = thbViolationCount; }
        public Integer getNoMemberCodeCount() { return noMemberCodeCount; }
        public void setNoMemberCodeCount(Integer noMemberCodeCount) { this.noMemberCodeCount = noMemberCodeCount; }
        public List<BusinessRuleViolationDetail> getViolationDetails() { return violationDetails; }
        public void setViolationDetails(List<BusinessRuleViolationDetail> violationDetails) { this.violationDetails = violationDetails; }
    }
    
    public static class BusinessRuleViolationDetail {
        private String orderNumber;
        private String violationType;
        private String description;
        private LocalDateTime createdAt;
        
        // Getters and Setters
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public String getViolationType() { return violationType; }
        public void setViolationType(String violationType) { this.violationType = violationType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}