package com.ycs.lms.repository;

import com.ycs.lms.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {
    
    /**
     * 주문 ID로 결제 정보 찾기
     */
    Optional<Billing> findByOrderId(Long orderId);
    
    /**
     * 결제 상태별 결제 정보 조회
     */
    List<Billing> findByPaymentStatus(Billing.PaymentStatus paymentStatus);
    
    /**
     * 1차 견적서가 발행된 결제 정보 조회
     */
    List<Billing> findByProformaIssuedTrue();
    
    /**
     * 2차 견적서가 발행된 결제 정보 조회
     */
    List<Billing> findByFinalIssuedTrue();
    
    /**
     * 특정 기간 내 생성된 결제 정보 조회
     */
    @Query("SELECT b FROM Billing b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<Billing> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * 결제 완료된 건의 총액 합계 조회 (특정 기간)
     */
    @Query("SELECT COALESCE(SUM(b.total), 0) FROM Billing b WHERE b.paymentStatus = 'COMPLETED' " +
           "AND b.paymentDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumTotalByPaymentDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * 사용자별 결제 정보 조회
     */
    @Query("SELECT b FROM Billing b JOIN b.order o WHERE o.user.id = :userId ORDER BY b.createdAt DESC")
    List<Billing> findByUserId(@Param("userId") Long userId);
    
    /**
     * 미결제 건 조회 (결제 대기 중)
     */
    @Query("SELECT b FROM Billing b WHERE b.paymentStatus = 'PENDING' AND b.proformaIssued = true " +
           "ORDER BY b.proformaDate ASC")
    List<Billing> findPendingPayments();
    
    /**
     * 결제 방법별 결제 건수 조회
     */
    @Query("SELECT b.paymentMethod, COUNT(b) FROM Billing b WHERE b.paymentStatus = 'COMPLETED' " +
           "GROUP BY b.paymentMethod")
    List<Object[]> countByPaymentMethod();
    
    /**
     * 월별 매출 조회
     */
    @Query("SELECT FUNCTION('YEAR', b.paymentDate), FUNCTION('MONTH', b.paymentDate), " +
           "COUNT(b), COALESCE(SUM(b.total), 0) " +
           "FROM Billing b WHERE b.paymentStatus = 'COMPLETED' " +
           "GROUP BY FUNCTION('YEAR', b.paymentDate), FUNCTION('MONTH', b.paymentDate) " +
           "ORDER BY FUNCTION('YEAR', b.paymentDate) DESC, FUNCTION('MONTH', b.paymentDate) DESC")
    List<Object[]> getMonthlySalesReport();
}