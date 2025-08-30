package com.ycs.lms.repository;

import com.ycs.lms.entity.InboundLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * YCS 접수지 정보 레포지토리
 */
@Repository
public interface InboundLocationRepository extends JpaRepository<InboundLocation, Long> {
    
    /**
     * 활성 상태별 조회 (표시 순서별 정렬)
     */
    Page<InboundLocation> findByIsActiveOrderByDisplayOrder(Boolean isActive, Pageable pageable);
    
    /**
     * 전체 조회 (표시 순서별 정렬)
     */
    Page<InboundLocation> findAllByOrderByDisplayOrder(Pageable pageable);
    
    /**
     * 활성 접수지 목록 (표시 순서별)
     */
    List<InboundLocation> findByIsActiveTrueOrderByDisplayOrder();
    
    /**
     * 이름/주소 검색 (활성 상태 필터 포함)
     */
    Page<InboundLocation> findByIsActiveAndNameContainingOrAddressContainingOrderByDisplayOrder(
        Boolean isActive, String nameKeyword, String addressKeyword, Pageable pageable);
    
    /**
     * 이름/주소 검색 (전체)
     */
    Page<InboundLocation> findByNameContainingOrAddressContainingOrderByDisplayOrder(
        String nameKeyword, String addressKeyword, Pageable pageable);
    
    /**
     * 이름 중복 확인
     */
    boolean existsByName(String name);
    
    /**
     * 최대 표시 순서 조회
     */
    @Query("SELECT MAX(il.displayOrder) FROM InboundLocation il")
    Integer findMaxDisplayOrder();
    
    /**
     * 특정 접수지를 사용하는 주문 개수 확인 (임시로 0 반환)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE 1=0")
    long countOrdersUsingLocation(@Param("locationId") Long locationId);
    
    /**
     * 표시 순서별 활성 접수지 (제한된 개수)
     */
    List<InboundLocation> findTop10ByIsActiveTrueOrderByDisplayOrder();
    
    /**
     * 특정 지역 접수지 검색
     */
    @Query("SELECT il FROM InboundLocation il WHERE il.isActive = true AND " +
           "(il.address LIKE %:region% OR il.name LIKE %:region%) " +
           "ORDER BY il.displayOrder")
    List<InboundLocation> findByRegion(@Param("region") String region);
    
    /**
     * 연락처 기준 검색
     */
    List<InboundLocation> findByPhoneContainingAndIsActiveTrue(String phone);
    
    /**
     * 담당자 기준 검색
     */
    List<InboundLocation> findByContactPersonContainingAndIsActiveTrue(String contactPerson);
    
    /**
     * 특정 표시 순서 범위의 접수지들
     */
    @Query("SELECT il FROM InboundLocation il WHERE il.displayOrder BETWEEN :startOrder AND :endOrder " +
           "ORDER BY il.displayOrder")
    List<InboundLocation> findByDisplayOrderRange(@Param("startOrder") Integer startOrder, 
                                                  @Param("endOrder") Integer endOrder);
    
    /**
     * 운영시간이 설정된 접수지들
     */
    @Query("SELECT il FROM InboundLocation il WHERE il.isActive = true AND il.businessHours IS NOT NULL " +
           "ORDER BY il.displayOrder")
    List<InboundLocation> findActiveLocationsWithBusinessHours();
    
    /**
     * 특별 안내사항이 있는 접수지들
     */
    @Query("SELECT il FROM InboundLocation il WHERE il.isActive = true AND il.specialInstructions IS NOT NULL " +
           "AND LENGTH(il.specialInstructions) > 0 ORDER BY il.displayOrder")
    List<InboundLocation> findActiveLocationsWithSpecialInstructions();
    
    /**
     * 최근 생성된 접수지들 (최신순)
     */
    List<InboundLocation> findTop5ByOrderByCreatedAtDesc();
    
    /**
     * 최근 수정된 접수지들 (최신순)
     */
    List<InboundLocation> findTop5ByOrderByUpdatedAtDesc();
    
    /**
     * 통계용 - 활성/비활성 개수
     */
    @Query("SELECT il.isActive, COUNT(il) FROM InboundLocation il GROUP BY il.isActive")
    List<Object[]> countByActiveStatus();
}