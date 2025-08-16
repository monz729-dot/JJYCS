package com.ycs.lms.mapper;

import com.ycs.lms.entity.EnterpriseProfile;
import com.ycs.lms.entity.PartnerProfile;
import com.ycs.lms.entity.User;
import com.ycs.lms.entity.WarehouseProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 MyBatis 매퍼 인터페이스
 * XML 파일에서 구현됨: UserMapper.xml
 */
@Mapper
public interface UserMapper {

    // User CRUD operations
    void insertUser(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByMemberCode(String memberCode);
    boolean existsByMemberCode(String memberCode);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    List<User> findByStatus(String status);
    
    // Update operations
    void updateUser(User user);
    void verifyEmail(@Param("id") Long id, @Param("emailVerified") boolean emailVerified);
    void updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);
    void updateLastLogin(Long id);
    void incrementLoginAttempts(Long id);
    void approveUser(@Param("id") Long id, @Param("memberCode") String memberCode);
    
    // Delete operations
    void deleteById(Long id);

    // Admin operations
    List<User> findPendingApprovals(@Param("limit") int limit, @Param("offset") int offset);
    long countPendingApprovals();
    
    // JPA Repository 호환 메서드 (Service 레이어 마이그레이션 용이성을 위해 추가)
    default User save(User user) {
        if (user.getId() == null) {
            insertUser(user);
        } else {
            updateUser(user);
        }
        return user;
    }
    
    default List<User> findAll() {
        throw new UnsupportedOperationException("Use specific find methods instead");
    }
}