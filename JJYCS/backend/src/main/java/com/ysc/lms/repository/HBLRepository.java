package com.ysc.lms.repository;

import com.ysc.lms.entity.HBL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * P0-4: HBL Repository
 */
@Repository
public interface HBLRepository extends JpaRepository<HBL, Long> {
    
    /**
     * HBL 번호로 조회
     */
    Optional<HBL> findByHblNumber(String hblNumber);
    
    /**
     * 주문 ID로 HBL 조회
     */
    Optional<HBL> findByOrderId(Long orderId);
    
    /**
     * HBL 상태별 조회
     */
    List<HBL> findByStatus(HBL.HblStatus status);
    
    /**
     * 운송 방식별 조회
     */
    List<HBL> findByTransportMode(HBL.TransportMode transportMode);
    
    /**
     * 발행일 기준 조회
     */
    @Query("SELECT h FROM HBL h WHERE h.issuedAt BETWEEN :startDate AND :endDate ORDER BY h.issuedAt DESC")
    List<HBL> findByIssuedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * 발행 대기 중인 HBL 조회 (승인 완료되었으나 미발행)
     */
    @Query("SELECT h FROM HBL h WHERE h.status = 'APPROVED' ORDER BY h.updatedAt ASC")
    List<HBL> findPendingIssuance();
    
    /**
     * 인쇄 대기 중인 HBL 조회
     */
    @Query("SELECT h FROM HBL h WHERE h.status = 'ISSUED' AND h.printedAt IS NULL ORDER BY h.issuedAt ASC")
    List<HBL> findPendingPrint();
    
    /**
     * 발송인/수취인으로 검색
     */
    @Query("SELECT h FROM HBL h WHERE h.shipperName LIKE %:keyword% OR h.consigneeName LIKE %:keyword%")
    List<HBL> findByShipperOrConsigneeName(@Param("keyword") String keyword);
    
    /**
     * 선적항/양하항으로 검색
     */
    @Query("SELECT h FROM HBL h WHERE h.portOfLoading LIKE %:port% OR h.portOfDischarge LIKE %:port%")
    List<HBL> findByPort(@Param("port") String port);
    
    /**
     * HBL 번호 생성을 위한 최신 번호 조회
     */
    @Query(value = "SELECT hbl_number FROM hbls WHERE hbl_number LIKE :prefix ORDER BY hbl_number DESC LIMIT 1", 
           nativeQuery = true)
    String findMaxHblNumberByPrefix(@Param("prefix") String prefix);
    
    /**
     * 연도별 HBL 발행 통계
     */
    @Query("SELECT FUNCTION('YEAR', h.issuedAt), COUNT(h) FROM HBL h " +
           "WHERE h.status IN ('ISSUED', 'PRINTED', 'DELIVERED') " +
           "GROUP BY FUNCTION('YEAR', h.issuedAt) " +
           "ORDER BY FUNCTION('YEAR', h.issuedAt) DESC")
    List<Object[]> getYearlyIssuanceStatistics();
    
    /**
     * 월별 HBL 발행 통계
     */
    @Query("SELECT FUNCTION('YEAR', h.issuedAt), FUNCTION('MONTH', h.issuedAt), COUNT(h) FROM HBL h " +
           "WHERE h.status IN ('ISSUED', 'PRINTED', 'DELIVERED') " +
           "GROUP BY FUNCTION('YEAR', h.issuedAt), FUNCTION('MONTH', h.issuedAt) " +
           "ORDER BY FUNCTION('YEAR', h.issuedAt) DESC, FUNCTION('MONTH', h.issuedAt) DESC")
    List<Object[]> getMonthlyIssuanceStatistics();
    
    /**
     * 운송 방식별 HBL 통계
     */
    @Query("SELECT h.transportMode, COUNT(h) FROM HBL h " +
           "WHERE h.status IN ('ISSUED', 'PRINTED', 'DELIVERED') " +
           "GROUP BY h.transportMode")
    List<Object[]> getTransportModeStatistics();
    
    /**
     * 항구별 HBL 통계
     */
    @Query("SELECT h.portOfDischarge, COUNT(h) FROM HBL h " +
           "WHERE h.status IN ('ISSUED', 'PRINTED', 'DELIVERED') " +
           "GROUP BY h.portOfDischarge " +
           "ORDER BY COUNT(h) DESC")
    List<Object[]> getPortStatistics();
    
    /**
     * 사용자별 HBL 조회 (주문을 통해)
     */
    @Query("SELECT h FROM HBL h JOIN h.order o WHERE o.user.id = :userId ORDER BY h.createdAt DESC")
    List<HBL> findByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자별 HBL 조회 (페이징)
     */
    @Query("SELECT h FROM HBL h JOIN h.order o WHERE o.user.id = :userId")
    Page<HBL> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * QR 코드가 생성된 HBL 조회
     */
    @Query("SELECT h FROM HBL h WHERE h.qrCodeData IS NOT NULL AND h.qrCodePath IS NOT NULL")
    List<HBL> findWithQrCode();
    
    /**
     * 라벨이 생성된 HBL 조회
     */
    @Query("SELECT h FROM HBL h WHERE SIZE(h.labelPaths) > 0")
    List<HBL> findWithLabels();
    
    /**
     * 특정 CBM 범위의 HBL 조회
     */
    @Query("SELECT h FROM HBL h WHERE h.totalCbm BETWEEN :minCbm AND :maxCbm")
    List<HBL> findByCbmRange(@Param("minCbm") java.math.BigDecimal minCbm, 
                            @Param("maxCbm") java.math.BigDecimal maxCbm);
    
    /**
     * 특정 중량 범위의 HBL 조회
     */
    @Query("SELECT h FROM HBL h WHERE h.grossWeight BETWEEN :minWeight AND :maxWeight")
    List<HBL> findByWeightRange(@Param("minWeight") java.math.BigDecimal minWeight, 
                               @Param("maxWeight") java.math.BigDecimal maxWeight);
    
    /**
     * 중복 HBL 번호 체크
     */
    boolean existsByHblNumber(String hblNumber);
}