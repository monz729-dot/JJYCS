package com.ycs.lms.repository;

import com.ycs.lms.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    Page<Invoice> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<Invoice> findByStatusOrderByCreatedAtDesc(Invoice.InvoiceStatus status, Pageable pageable);
    
    List<Invoice> findByStatusAndDueDateBeforeOrderByDueDateAsc(
        Invoice.InvoiceStatus status, LocalDateTime dueDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId AND i.status = :status")
    List<Invoice> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Invoice.InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.order.id = :orderId AND i.invoiceType = :type")
    Optional<Invoice> findByOrderIdAndInvoiceType(@Param("orderId") Long orderId, @Param("type") Invoice.InvoiceType type);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") Invoice.InvoiceStatus status);
    
    @Query("SELECT SUM(i.paidAmount) FROM Invoice i WHERE i.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPaidAmountBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    Long countByStatus(@Param("status") Invoice.InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.status IN ('PAYMENT_PENDING', 'PARTIALLY_PAID') AND i.dueDate < :now")
    List<Invoice> findOverdueInvoices(@Param("now") LocalDateTime now);
    
    // 월별 매출 통계
    @Query("SELECT MONTH(i.paidAt) as month, SUM(i.paidAmount) as totalPaid " +
           "FROM Invoice i WHERE YEAR(i.paidAt) = :year AND i.status = 'FULLY_PAID' " +
           "GROUP BY MONTH(i.paidAt) ORDER BY month")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);
}