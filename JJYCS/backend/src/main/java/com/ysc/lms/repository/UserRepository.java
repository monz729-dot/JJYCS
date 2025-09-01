package com.ysc.lms.repository;

import com.ysc.lms.entity.User;
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
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByMemberCode(String memberCode);
    
    boolean existsByEmail(String email);
    
    boolean existsByMemberCode(String memberCode);
    
    List<User> findByUserType(User.UserType userType);
    
    List<User> findByStatus(User.UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.status = :status")
    List<User> findByUserTypeAndStatus(@Param("userType") User.UserType userType, 
                                      @Param("status") User.UserStatus status);
    
    // findPartnersByRegion method removed (partnerRegion field removed)
    
    Optional<User> findByEmailVerificationToken(String token);
    
    // 승인 관련 메서드
    List<User> findByStatusOrderByCreatedAtDesc(User.UserStatus status);
    
    Page<User> findByStatusOrderByCreatedAtDesc(User.UserStatus status, Pageable pageable);
    
    List<User> findByStatusAndUserTypeOrderByCreatedAtDesc(User.UserStatus status, User.UserType userType);
    
    Page<User> findByStatusAndUserTypeOrderByCreatedAtDesc(User.UserStatus status, User.UserType userType, Pageable pageable);
    
    Long countByStatus(User.UserStatus status);
    
    Long countByStatusAndUserType(User.UserStatus status, User.UserType userType);
    
    // 관리자용 사용자 관리
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<User> findByUserTypeOrderByCreatedAtDesc(User.UserType userType, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% OR u.email LIKE %:keyword% OR u.companyName LIKE %:keyword% ORDER BY u.createdAt DESC")
    Page<User> findByKeywordOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);
    
    // 파트너 리퍼럴 기능은 제거됨
    
    // 코드 생성을 위한 최대 시퀀스 조회
    @Query(value = "SELECT member_code FROM users WHERE member_code LIKE :prefix% ORDER BY member_code DESC LIMIT 1", nativeQuery = true)
    String findMaxMemberCodeByPrefix(@Param("prefix") String prefix);
    
    // 비밀번호 찾기를 위한 사용자 검색 (이름, 연락처, 이메일로 검색)
    Optional<User> findByEmailAndNameAndPhone(String email, String name, String phone);
    
    // AdminController에서 필요한 추가 메소드들
    Long countByUserType(User.UserType userType);
    Long countByCreatedAtAfter(LocalDateTime date);
    List<User> findTop10ByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);
    List<User> findByStatusAndUserType(User.UserStatus status, User.UserType userType);
}