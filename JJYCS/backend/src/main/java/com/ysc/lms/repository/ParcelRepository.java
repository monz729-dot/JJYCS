package com.ysc.lms.repository;

import com.ysc.lms.entity.Parcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    
    // 주문별 택배 조회
    List<Parcel> findByOrderId(Long orderId);
    
    // 송장번호로 조회
    Optional<Parcel> findByTrackingNumber(String trackingNumber);
    
    // 택배사와 송장번호로 조회
    Optional<Parcel> findByCarrierAndTrackingNumber(String carrier, String trackingNumber);
    
    // 검증 상태별 조회
    List<Parcel> findByValidationStatus(Parcel.ValidationStatus validationStatus);
    
    // 미매칭 택배 조회 (입고 시 수동 매칭용)
    @Query("SELECT p FROM Parcel p WHERE p.isMatched = false AND p.inboundScanAt IS NOT NULL")
    List<Parcel> findUnmatchedInboundParcels();
    
    // 검증이 필요한 택배들 (PENDING 상태이면서 일정 시간 경과)
    @Query("SELECT p FROM Parcel p WHERE p.validationStatus = 'PENDING' AND p.createdAt < :cutoffTime")
    List<Parcel> findPendingValidationParcels(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // 수취인/연락처로 검색 (수기 매칭용)
    @Query("SELECT p FROM Parcel p JOIN p.order o WHERE " +
           "(o.recipientName LIKE %:keyword% OR o.recipientPhone LIKE %:keyword%) " +
           "AND p.isMatched = false")
    List<Parcel> findUnmatchedByRecipientKeyword(@Param("keyword") String keyword);
    
    // 품목 키워드로 검색 (수기 매칭용)
    @Query("SELECT p FROM Parcel p JOIN p.order o JOIN o.items i WHERE " +
           "(i.description LIKE %:keyword%) " +
           "AND p.isMatched = false")
    List<Parcel> findUnmatchedByItemKeyword(@Param("keyword") String keyword);
    
    // 택배사별 통계
    @Query("SELECT p.carrier, COUNT(p) FROM Parcel p GROUP BY p.carrier")
    List<Object[]> findCarrierStatistics();
    
    // 검증 상태별 통계
    @Query("SELECT p.validationStatus, COUNT(p) FROM Parcel p GROUP BY p.validationStatus")
    List<Object[]> findValidationStatusStatistics();
    
    // 최근 검증된 택배들 (페이징)
    Page<Parcel> findByLastValidatedAtIsNotNullOrderByLastValidatedAtDesc(Pageable pageable);
}