package com.ysc.lms.repository;

import com.ysc.lms.entity.CourierCompany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 택배사 정보 레포지토리
 */
@Repository
public interface CourierCompanyRepository extends JpaRepository<CourierCompany, Long> {
    
    /**
     * 코드로 택배사 조회
     */
    Optional<CourierCompany> findByCode(String code);
    
    /**
     * 코드 중복 확인
     */
    boolean existsByCode(String code);
    
    /**
     * 활성 상태별 조회 (표시 순서별 정렬)
     */
    Page<CourierCompany> findByIsActiveOrderByDisplayOrder(Boolean isActive, Pageable pageable);
    
    /**
     * 전체 조회 (표시 순서별 정렬)
     */
    Page<CourierCompany> findAllByOrderByDisplayOrder(Pageable pageable);
    
    /**
     * 활성 택배사 목록 (표시 순서별)
     */
    List<CourierCompany> findByIsActiveTrueOrderByDisplayOrder();
    
    /**
     * 이름/코드 검색 (활성 상태 필터 포함)
     */
    @Query("SELECT cc FROM CourierCompany cc WHERE cc.isActive = :isActive AND " +
           "(cc.name LIKE %:keyword% OR cc.code LIKE %:keyword% OR cc.nameEn LIKE %:keyword%) " +
           "ORDER BY cc.displayOrder")
    Page<CourierCompany> findByIsActiveAndSearchKeyword(@Param("isActive") Boolean isActive, 
                                                       @Param("keyword") String keyword, 
                                                       Pageable pageable);
    
    /**
     * 이름/코드 검색 (전체)
     */
    @Query("SELECT cc FROM CourierCompany cc WHERE " +
           "(cc.name LIKE %:keyword% OR cc.code LIKE %:keyword% OR cc.nameEn LIKE %:keyword%) " +
           "ORDER BY cc.displayOrder")
    Page<CourierCompany> findBySearchKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 최대 표시 순서 조회
     */
    @Query("SELECT MAX(cc.displayOrder) FROM CourierCompany cc")
    Integer findMaxDisplayOrder();
    
    /**
     * 특정 택배사를 사용하는 주문 개수 확인 (임시로 0 반환)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE 1=0")
    long countOrdersUsingCourier(@Param("courierCompany") String courierCompany);
    
    /**
     * 추적 URL 지원 택배사들
     */
    @Query("SELECT cc FROM CourierCompany cc WHERE cc.isActive = true AND " +
           "cc.trackingUrlTemplate IS NOT NULL AND cc.trackingUrlTemplate != '' " +
           "ORDER BY cc.displayOrder")
    List<CourierCompany> findActiveWithTrackingUrl();
    
    /**
     * 특정 표시 순서 범위의 택배사들
     */
    @Query("SELECT cc FROM CourierCompany cc WHERE cc.displayOrder BETWEEN :startOrder AND :endOrder " +
           "ORDER BY cc.displayOrder")
    List<CourierCompany> findByDisplayOrderRange(@Param("startOrder") Integer startOrder, 
                                                @Param("endOrder") Integer endOrder);
    
    /**
     * 웹사이트가 있는 택배사들
     */
    @Query("SELECT cc FROM CourierCompany cc WHERE cc.isActive = true AND cc.website IS NOT NULL " +
           "AND LENGTH(cc.website) > 0 ORDER BY cc.displayOrder")
    List<CourierCompany> findActiveWithWebsite();
    
    /**
     * 영문명이 있는 택배사들
     */
    @Query("SELECT cc FROM CourierCompany cc WHERE cc.isActive = true AND cc.nameEn IS NOT NULL " +
           "AND LENGTH(cc.nameEn) > 0 ORDER BY cc.displayOrder")
    List<CourierCompany> findActiveWithEnglishName();
    
    /**
     * 최근 생성된 택배사들 (최신순)
     */
    List<CourierCompany> findTop5ByOrderByCreatedAtDesc();
    
    /**
     * 최근 수정된 택배사들 (최신순)
     */
    List<CourierCompany> findTop5ByOrderByUpdatedAtDesc();
    
    /**
     * 통계용 - 활성/비활성 개수
     */
    @Query("SELECT cc.isActive, COUNT(cc) FROM CourierCompany cc GROUP BY cc.isActive")
    List<Object[]> countByActiveStatus();
    
    /**
     * 코드 패턴으로 검색
     */
    @Query("SELECT cc FROM CourierCompany cc WHERE cc.code LIKE :pattern ORDER BY cc.displayOrder")
    List<CourierCompany> findByCodePattern(@Param("pattern") String pattern);
    
    /**
     * 이름 패턴으로 검색 (대소문자 무관)
     */
    @Query("SELECT cc FROM CourierCompany cc WHERE LOWER(cc.name) LIKE LOWER(:pattern) " +
           "OR LOWER(cc.nameEn) LIKE LOWER(:pattern) ORDER BY cc.displayOrder")
    List<CourierCompany> findByNamePattern(@Param("pattern") String pattern);
}