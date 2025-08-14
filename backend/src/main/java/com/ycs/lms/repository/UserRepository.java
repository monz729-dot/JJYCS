package com.ycs.lms.repository;

import com.ycs.lms.entity.User;
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
    
    boolean existsByEmail(String email);
    
    boolean existsByMemberCode(String memberCode);
    
    Optional<User> findByMemberCode(String memberCode);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    Optional<User> findByPasswordResetToken(String token);
    
    List<User> findByStatus(User.UserStatus status);
    
    List<User> findByRole(User.UserRole role);
    
    List<User> findByStatusAndRole(User.UserStatus status, User.UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.createdAt >= :since")
    List<User> findPendingUsersCreatedSince(@Param("status") User.UserStatus status, @Param("since") LocalDateTime since);
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :before")
    List<User> findUnverifiedUsersCreatedBefore(@Param("before") LocalDateTime before);
    
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL OR u.lastLoginAt < :before")
    List<User> findInactiveUsersSince(@Param("before") LocalDateTime before);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") User.UserStatus status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.UserRole role);
}