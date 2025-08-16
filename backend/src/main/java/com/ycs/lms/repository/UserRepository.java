package com.ycs.lms.repository;

import com.ycs.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 리포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 회원코드로 사용자 조회
     */
    Optional<User> findByMemberCode(String memberCode);
    
    /**
     * 회원코드 존재 여부 확인
     */
    boolean existsByMemberCode(String memberCode);
    
    /**
     * 이메일 검증 토큰으로 사용자 조회
     */
    Optional<User> findByEmailVerificationToken(String token);
    
    /**
     * 비밀번호 리셋 토큰으로 사용자 조회
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * 상태별 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.status = :status")
    java.util.List<User> findByStatus(@Param("status") String status);
}