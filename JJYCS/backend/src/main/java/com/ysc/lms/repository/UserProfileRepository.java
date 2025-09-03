package com.ysc.lms.repository;

import com.ysc.lms.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    
    // 이메일로 프로필 조회
    Optional<UserProfile> findByEmail(String email);
    
    // 사용자명으로 프로필 조회
    Optional<UserProfile> findByUsername(String username);
    
    // 사용자 타입별 조회
    List<UserProfile> findByUserType(String userType);
    
    // 승인 상태별 조회
    List<UserProfile> findByApprovalStatus(String approvalStatus);
    
    // 승인 대기중인 사용자들 (페이징)
    Page<UserProfile> findByApprovalStatusOrderByCreatedAtDesc(String approvalStatus, Pageable pageable);
    
    // 관리자용: 승인 대기 중인 기업/파트너 사용자들
    @Query("SELECT up FROM UserProfile up WHERE up.approvalStatus = 'PENDING' AND up.userType IN ('CORPORATE', 'PARTNER') ORDER BY up.createdAt DESC")
    List<UserProfile> findPendingCorporateAndPartners();
    
    // 통계용: 사용자 타입별 개수
    @Query("SELECT up.userType, COUNT(up) FROM UserProfile up GROUP BY up.userType")
    List<Object[]> countByUserType();
    
    // 통계용: 승인 상태별 개수
    @Query("SELECT up.approvalStatus, COUNT(up) FROM UserProfile up GROUP BY up.approvalStatus")
    List<Object[]> countByApprovalStatus();
    
    // 검색: 이름, 이메일, 회사명으로 검색
    @Query("SELECT up FROM UserProfile up WHERE " +
           "LOWER(up.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(up.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(up.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UserProfile> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}