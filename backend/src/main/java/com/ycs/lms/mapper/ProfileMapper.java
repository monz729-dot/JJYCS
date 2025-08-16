package com.ycs.lms.mapper;

import com.ycs.lms.entity.EnterpriseProfile;
import com.ycs.lms.entity.PartnerProfile;
import com.ycs.lms.entity.WarehouseProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 프로필 MyBatis 매퍼 인터페이스
 * XML 파일에서 구현됨: ProfileMapper.xml
 */
@Mapper
public interface ProfileMapper {

    // Enterprise Profile operations
    void insertEnterpriseProfile(EnterpriseProfile profile);
    EnterpriseProfile findEnterpriseProfileById(Long id);
    EnterpriseProfile findEnterpriseProfileByUserId(Long userId);
    void updateEnterpriseProfile(EnterpriseProfile profile);
    void deleteEnterpriseProfile(Long id);
    void deleteEnterpriseProfileByUserId(Long userId);

    // Partner Profile operations
    void insertPartnerProfile(PartnerProfile profile);
    PartnerProfile findPartnerProfileById(Long id);
    PartnerProfile findPartnerProfileByUserId(Long userId);
    PartnerProfile findPartnerProfileByReferralCode(String referralCode);
    void updatePartnerProfile(PartnerProfile profile);
    void deletePartnerProfile(Long id);
    void deletePartnerProfileByUserId(Long userId);

    // Warehouse Profile operations
    void insertWarehouseProfile(WarehouseProfile profile);
    WarehouseProfile findWarehouseProfileById(Long id);
    WarehouseProfile findWarehouseProfileByUserId(Long userId);
    void updateWarehouseProfile(WarehouseProfile profile);
    void deleteWarehouseProfile(Long id);
    void deleteWarehouseProfileByUserId(Long userId);
}