package com.ycs.lms.mapper;

import com.ycs.lms.entity.EnterpriseProfile;
import com.ycs.lms.entity.PartnerProfile;
import com.ycs.lms.entity.User;
import com.ycs.lms.entity.WarehouseProfile;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    // User CRUD operations
    @Insert("""
        INSERT INTO users (email, password_hash, name, phone, role, status, 
                          email_verified, two_factor_enabled, agree_terms, agree_privacy, agree_marketing)
        VALUES (#{email}, #{passwordHash}, #{name}, #{phone}, #{role}, #{status},
                #{emailVerified}, #{twoFactorEnabled}, #{agreeTerms}, #{agreePrivacy}, #{agreeMarketing})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(User user);

    @Select("""
        SELECT * FROM users WHERE id = #{id}
    """)
    @Results(id = "userResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "email", column = "email"),
        @Result(property = "passwordHash", column = "password_hash"),
        @Result(property = "memberCode", column = "member_code"),
        @Result(property = "emailVerified", column = "email_verified"),
        @Result(property = "emailVerificationToken", column = "email_verification_token"),
        @Result(property = "twoFactorEnabled", column = "two_factor_enabled"),
        @Result(property = "twoFactorSecret", column = "two_factor_secret"),
        @Result(property = "passwordResetToken", column = "password_reset_token"),
        @Result(property = "passwordResetExpiresAt", column = "password_reset_expires_at"),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "loginAttempts", column = "login_attempts"),
        @Result(property = "lockedUntil", column = "locked_until"),
        @Result(property = "agreeTerms", column = "agree_terms"),
        @Result(property = "agreePrivacy", column = "agree_privacy"),
        @Result(property = "agreeMarketing", column = "agree_marketing"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<User> findById(Long id);

    @Select("""
        SELECT * FROM users WHERE email = #{email}
    """)
    @ResultMap("userResultMap")
    Optional<User> findByEmail(String email);

    @Select("""
        SELECT * FROM users WHERE member_code = #{memberCode}
    """)
    @ResultMap("userResultMap")
    Optional<User> findByMemberCode(String memberCode);

    @Update("""
        UPDATE users SET email_verified = #{emailVerified}, email_verification_token = NULL, updated_at = NOW()
        WHERE id = #{id}
    """)
    void verifyEmail(@Param("id") Long id, @Param("emailVerified") boolean emailVerified);

    @Update("""
        UPDATE users SET password_hash = #{passwordHash}, password_reset_token = NULL, 
                        password_reset_expires_at = NULL, updated_at = NOW()
        WHERE id = #{id}
    """)
    void updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    @Update("""
        UPDATE users SET last_login_at = NOW(), login_attempts = 0, locked_until = NULL, updated_at = NOW()
        WHERE id = #{id}
    """)
    void updateLastLogin(Long id);

    @Update("""
        UPDATE users SET login_attempts = login_attempts + 1,
                        locked_until = CASE 
                            WHEN login_attempts >= 4 THEN DATE_ADD(NOW(), INTERVAL 30 MINUTE)
                            ELSE locked_until
                        END,
                        updated_at = NOW()
        WHERE id = #{id}
    """)
    void incrementLoginAttempts(Long id);

    @Update("""
        UPDATE users SET member_code = #{memberCode}, status = 'active', updated_at = NOW()
        WHERE id = #{id}
    """)
    void approveUser(@Param("id") Long id, @Param("memberCode") String memberCode);

    // Enterprise Profile operations
    @Insert("""
        INSERT INTO enterprise_profiles (user_id, company_name, business_number, company_address, ceo_name, business_type, employee_count)
        VALUES (#{userId}, #{companyName}, #{businessNumber}, #{companyAddress}, #{ceoName}, #{businessType}, #{employeeCount})
    """)
    void insertEnterpriseProfile(EnterpriseProfile profile);

    @Select("""
        SELECT * FROM enterprise_profiles WHERE user_id = #{userId}
    """)
    @Results({
        @Result(property = "userId", column = "user_id"),
        @Result(property = "companyName", column = "company_name"),
        @Result(property = "businessNumber", column = "business_number"),
        @Result(property = "companyAddress", column = "company_address"),
        @Result(property = "ceoName", column = "ceo_name"),
        @Result(property = "businessType", column = "business_type"),
        @Result(property = "employeeCount", column = "employee_count"),
        @Result(property = "annualRevenue", column = "annual_revenue"),
        @Result(property = "businessRegistrationFile", column = "business_registration_file"),
        @Result(property = "taxCertificateFile", column = "tax_certificate_file"),
        @Result(property = "approvalNote", column = "approval_note"),
        @Result(property = "approvedBy", column = "approved_by"),
        @Result(property = "approvedAt", column = "approved_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<EnterpriseProfile> findEnterpriseProfileByUserId(Long userId);

    // Partner Profile operations
    @Insert("""
        INSERT INTO partner_profiles (user_id, partner_type, referral_code, bank_name, account_number, account_holder, commission_rate)
        VALUES (#{userId}, #{partnerType}, #{referralCode}, #{bankName}, #{accountNumber}, #{accountHolder}, #{commissionRate})
    """)
    void insertPartnerProfile(PartnerProfile profile);

    @Select("""
        SELECT * FROM partner_profiles WHERE user_id = #{userId}
    """)
    @Results({
        @Result(property = "userId", column = "user_id"),
        @Result(property = "partnerType", column = "partner_type"),
        @Result(property = "referralCode", column = "referral_code"),
        @Result(property = "bankName", column = "bank_name"),
        @Result(property = "accountNumber", column = "account_number"),
        @Result(property = "accountHolder", column = "account_holder"),
        @Result(property = "commissionRate", column = "commission_rate"),
        @Result(property = "totalReferrals", column = "total_referrals"),
        @Result(property = "totalCommission", column = "total_commission"),
        @Result(property = "pendingCommission", column = "pending_commission"),
        @Result(property = "lastSettlementAt", column = "last_settlement_at"),
        @Result(property = "approvalNote", column = "approval_note"),
        @Result(property = "approvedBy", column = "approved_by"),
        @Result(property = "approvedAt", column = "approved_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<PartnerProfile> findPartnerProfileByUserId(Long userId);

    // Warehouse Profile operations
    @Insert("""
        INSERT INTO warehouse_profiles (user_id, warehouse_name, warehouse_address, capacity_description, 
                                       operating_hours, contact_person, contact_phone, facilities)
        VALUES (#{userId}, #{warehouseName}, #{warehouseAddress}, #{capacityDescription}, 
                #{operatingHours}, #{contactPerson}, #{contactPhone}, #{facilities})
    """)
    void insertWarehouseProfile(WarehouseProfile profile);

    @Select("""
        SELECT * FROM warehouse_profiles WHERE user_id = #{userId}
    """)
    @Results({
        @Result(property = "userId", column = "user_id"),
        @Result(property = "warehouseName", column = "warehouse_name"),
        @Result(property = "warehouseAddress", column = "warehouse_address"),
        @Result(property = "capacityDescription", column = "capacity_description"),
        @Result(property = "operatingHours", column = "operating_hours"),
        @Result(property = "contactPerson", column = "contact_person"),
        @Result(property = "contactPhone", column = "contact_phone"),
        @Result(property = "approvalNote", column = "approval_note"),
        @Result(property = "approvedBy", column = "approved_by"),
        @Result(property = "approvedAt", column = "approved_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<WarehouseProfile> findWarehouseProfileByUserId(Long userId);

    // Admin operations
    @Select("""
        SELECT u.*, ep.company_name, pp.referral_code, wp.warehouse_name
        FROM users u
        LEFT JOIN enterprise_profiles ep ON u.id = ep.user_id
        LEFT JOIN partner_profiles pp ON u.id = pp.user_id  
        LEFT JOIN warehouse_profiles wp ON u.id = wp.user_id
        WHERE u.status = 'pending_approval'
        ORDER BY u.created_at DESC
        LIMIT #{limit} OFFSET #{offset}
    """)
    List<User> findPendingApprovals(@Param("limit") int limit, @Param("offset") int offset);

    @Select("""
        SELECT COUNT(*) FROM users WHERE status = 'pending_approval'
    """)
    long countPendingApprovals();
}