package com.ysc.lms.repository;

import com.ysc.lms.entity.BulkItem;
import com.ysc.lms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BulkItemRepository extends JpaRepository<BulkItem, Long> {
    
    // 사용자별 품목 목록 조회
    Page<BulkItem> findByUserAndIsActiveOrderByCreatedAtDesc(User user, boolean isActive, Pageable pageable);
    
    // 사용자별 전체 품목 목록 조회 (비활성 포함)
    Page<BulkItem> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 사용자별 활성 품목 목록
    List<BulkItem> findByUserAndIsActiveTrueOrderByDescriptionAsc(User user);
    
    // 사용자별 품목 개수
    Long countByUserAndIsActive(User user, boolean isActive);
    
    // HS 코드로 검색
    List<BulkItem> findByUserAndHsCodeContainingAndIsActiveTrueOrderByDescriptionAsc(User user, String hsCode);
    
    // 품목명으로 검색
    @Query("SELECT bi FROM BulkItem bi WHERE bi.user = :user AND bi.isActive = true " +
           "AND (bi.description LIKE %:keyword% OR bi.englishName LIKE %:keyword% OR bi.hsCode LIKE %:keyword%) " +
           "ORDER BY bi.description ASC")
    List<BulkItem> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword);
    
    // 특정 사용자의 특정 품목 조회
    Optional<BulkItem> findByIdAndUser(Long id, User user);
    
    // 카테고리별 품목 통계
    @Query("SELECT bi.category, COUNT(bi) FROM BulkItem bi WHERE bi.user = :user AND bi.isActive = true " +
           "GROUP BY bi.category ORDER BY COUNT(bi) DESC")
    List<Object[]> getItemCountByCategory(@Param("user") User user);
    
    // HS 코드별 품목 통계
    @Query("SELECT bi.hsCode, COUNT(bi) FROM BulkItem bi WHERE bi.user = :user AND bi.isActive = true " +
           "GROUP BY bi.hsCode ORDER BY COUNT(bi) DESC")
    List<Object[]> getItemCountByHsCode(@Param("user") User user);
    
    // 중복 체크 (HS코드 + 품목명)
    Optional<BulkItem> findByUserAndHsCodeAndDescriptionAndIsActive(
        User user, String hsCode, String description, boolean isActive);
    
    // 카테고리별 품목 조회
    List<BulkItem> findByUserAndCategoryAndIsActiveTrueOrderByDescriptionAsc(User user, String category);
}