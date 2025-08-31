package com.ysc.lms.repository;

import com.ysc.lms.entity.BulkRecipient;
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
public interface BulkRecipientRepository extends JpaRepository<BulkRecipient, Long> {
    
    // 사용자별 수취인 목록 조회
    Page<BulkRecipient> findByUserAndIsActiveOrderByCreatedAtDesc(User user, boolean isActive, Pageable pageable);
    
    // 사용자별 전체 수취인 목록 조회 (비활성 포함)
    Page<BulkRecipient> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 사용자별 활성 수취인 목록
    List<BulkRecipient> findByUserAndIsActiveTrueOrderByRecipientNameAsc(User user);
    
    // 사용자별 수취인 개수
    Long countByUserAndIsActive(User user, boolean isActive);
    
    // 수취인 이름으로 검색
    @Query("SELECT br FROM BulkRecipient br WHERE br.user = :user AND br.isActive = true " +
           "AND (br.recipientName LIKE %:keyword% OR br.recipientAddress LIKE %:keyword% OR br.country LIKE %:keyword%) " +
           "ORDER BY br.recipientName ASC")
    List<BulkRecipient> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword);
    
    // 특정 사용자의 특정 수취인 조회
    Optional<BulkRecipient> findByIdAndUser(Long id, User user);
    
    // 국가별 수취인 통계
    @Query("SELECT br.country, COUNT(br) FROM BulkRecipient br WHERE br.user = :user AND br.isActive = true GROUP BY br.country ORDER BY COUNT(br) DESC")
    List<Object[]> getRecipientCountByCountry(@Param("user") User user);
    
    // 중복 체크 (이름 + 주소)
    Optional<BulkRecipient> findByUserAndRecipientNameAndRecipientAddressAndIsActive(
        User user, String recipientName, String recipientAddress, boolean isActive);
}