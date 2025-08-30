package com.ycs.lms.service;

import com.ycs.lms.dto.CourierCompanyDto;
import com.ycs.lms.entity.CourierCompany;
import com.ycs.lms.repository.CourierCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 택배사 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourierCompanyService {
    
    private final CourierCompanyRepository courierCompanyRepository;

    /**
     * 택배사 목록 조회 (페이징, 검색)
     */
    public Page<CourierCompanyDto> getCourierCompanies(Boolean isActive, String search, Pageable pageable) {
        Page<CourierCompany> companies;
        
        if (search != null && !search.trim().isEmpty()) {
            if (isActive != null) {
                companies = courierCompanyRepository.findByIsActiveAndSearchKeyword(
                    isActive, search.trim(), pageable);
            } else {
                companies = courierCompanyRepository.findBySearchKeyword(search.trim(), pageable);
            }
        } else {
            if (isActive != null) {
                companies = courierCompanyRepository.findByIsActiveOrderByDisplayOrder(isActive, pageable);
            } else {
                companies = courierCompanyRepository.findAllByOrderByDisplayOrder(pageable);
            }
        }
        
        return companies.map(this::convertToDto);
    }

    /**
     * 활성 택배사 목록 조회 (표시 순서별)
     */
    public List<CourierCompanyDto> getActiveCourierCompanies() {
        List<CourierCompany> companies = courierCompanyRepository.findByIsActiveTrueOrderByDisplayOrder();
        return companies.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * 택배사 상세 조회
     */
    public CourierCompanyDto getCourierCompanyById(Long id) {
        CourierCompany company = courierCompanyRepository.findById(id).orElse(null);
        return company != null ? convertToDto(company) : null;
    }

    /**
     * 코드로 택배사 조회
     */
    public CourierCompanyDto getCourierCompanyByCode(String code) {
        CourierCompany company = courierCompanyRepository.findByCode(code).orElse(null);
        return company != null ? convertToDto(company) : null;
    }

    /**
     * 새 택배사 등록
     */
    @Transactional
    public CourierCompanyDto createCourierCompany(CourierCompanyDto companyDto) {
        // 입력 검증
        validateCourierCompanyDto(companyDto);
        
        // 중복 코드 검증
        if (courierCompanyRepository.existsByCode(companyDto.getCode().trim().toUpperCase())) {
            throw new IllegalArgumentException("이미 동일한 코드의 택배사가 존재합니다.");
        }
        
        CourierCompany company = new CourierCompany();
        updateCompanyFromDto(company, companyDto);
        
        // 표시 순서 설정 (기존 최대값 + 1)
        if (companyDto.getDisplayOrder() == null || companyDto.getDisplayOrder() == 0) {
            Integer maxOrder = courierCompanyRepository.findMaxDisplayOrder();
            company.setDisplayOrder(maxOrder != null ? maxOrder + 1 : 1);
        }
        
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());
        
        CourierCompany saved = courierCompanyRepository.save(company);
        
        log.info("Created new courier company: {} [{}] (ID: {})", 
            saved.getName(), saved.getCode(), saved.getId());
        
        return convertToDto(saved);
    }

    /**
     * 택배사 정보 수정
     */
    @Transactional
    public CourierCompanyDto updateCourierCompany(Long id, CourierCompanyDto companyDto) {
        CourierCompany company = courierCompanyRepository.findById(id).orElse(null);
        
        if (company == null) {
            return null;
        }
        
        // 입력 검증
        validateCourierCompanyDto(companyDto);
        
        // 중복 코드 검증 (본인 제외)
        String newCode = companyDto.getCode().trim().toUpperCase();
        if (!company.getCode().equals(newCode) && 
            courierCompanyRepository.existsByCode(newCode)) {
            throw new IllegalArgumentException("이미 동일한 코드의 택배사가 존재합니다.");
        }
        
        updateCompanyFromDto(company, companyDto);
        company.setUpdatedAt(LocalDateTime.now());
        
        CourierCompany saved = courierCompanyRepository.save(company);
        
        log.info("Updated courier company: {} [{}] (ID: {})", 
            saved.getName(), saved.getCode(), saved.getId());
        
        return convertToDto(saved);
    }

    /**
     * 택배사 활성/비활성 토글
     */
    @Transactional
    public CourierCompanyDto toggleCourierCompanyStatus(Long id) {
        CourierCompany company = courierCompanyRepository.findById(id).orElse(null);
        
        if (company == null) {
            return null;
        }
        
        company.setIsActive(!company.getIsActive());
        company.setUpdatedAt(LocalDateTime.now());
        
        CourierCompany saved = courierCompanyRepository.save(company);
        
        log.info("Toggled courier company status: {} [{}] -> {} (ID: {})", 
            saved.getName(), saved.getCode(), saved.getIsActive() ? "ACTIVE" : "INACTIVE", saved.getId());
        
        return convertToDto(saved);
    }

    /**
     * 택배사 표시 순서 변경
     */
    @Transactional
    public CourierCompanyDto updateDisplayOrder(Long id, Integer displayOrder) {
        CourierCompany company = courierCompanyRepository.findById(id).orElse(null);
        
        if (company == null) {
            return null;
        }
        
        if (displayOrder == null || displayOrder < 0) {
            throw new IllegalArgumentException("표시 순서는 0 이상의 숫자여야 합니다.");
        }
        
        company.setDisplayOrder(displayOrder);
        company.setUpdatedAt(LocalDateTime.now());
        
        CourierCompany saved = courierCompanyRepository.save(company);
        
        log.info("Updated display order for courier company: {} [{}] -> {} (ID: {})", 
            saved.getName(), saved.getCode(), displayOrder, saved.getId());
        
        return convertToDto(saved);
    }

    /**
     * 택배사 삭제 (실제로는 비활성화)
     */
    @Transactional
    public boolean deleteCourierCompany(Long id) {
        CourierCompany company = courierCompanyRepository.findById(id).orElse(null);
        
        if (company == null) {
            return false;
        }
        
        // 사용 중인 택배사인지 확인 (주문에서 참조하고 있는지)
        long orderCount = courierCompanyRepository.countOrdersUsingCourier(company.getCode());
        
        if (orderCount > 0) {
            throw new IllegalStateException(
                String.format("해당 택배사는 %d개의 주문에서 사용 중이므로 삭제할 수 없습니다. 비활성화를 권장합니다.", orderCount));
        }
        
        // 실제 삭제 대신 비활성화
        company.setIsActive(false);
        company.setUpdatedAt(LocalDateTime.now());
        
        courierCompanyRepository.save(company);
        
        log.info("Deactivated courier company: {} [{}] (ID: {})", 
            company.getName(), company.getCode(), company.getId());
        
        return true;
    }

    /**
     * 송장번호 추적 URL 생성
     */
    public String generateTrackingUrl(String courierCode, String trackingNumber) {
        if (courierCode == null || trackingNumber == null) {
            return null;
        }
        
        CourierCompany company = courierCompanyRepository.findByCode(courierCode.toUpperCase()).orElse(null);
        return company != null ? company.generateTrackingUrl(trackingNumber) : null;
    }

    /**
     * 추적 기능 지원 택배사 목록
     */
    public List<CourierCompanyDto> getCourierCompaniesWithTracking() {
        List<CourierCompany> companies = courierCompanyRepository.findActiveWithTrackingUrl();
        return companies.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    // === 내부 메서드들 ===

    /**
     * Entity -> DTO 변환
     */
    private CourierCompanyDto convertToDto(CourierCompany entity) {
        return CourierCompanyDto.builder()
            .id(entity.getId())
            .code(entity.getCode())
            .name(entity.getName())
            .nameEn(entity.getNameEn())
            .website(entity.getWebsite())
            .trackingUrlTemplate(entity.getTrackingUrlTemplate())
            .isActive(entity.getIsActive())
            .displayOrder(entity.getDisplayOrder())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * DTO -> Entity 필드 복사
     */
    private void updateCompanyFromDto(CourierCompany entity, CourierCompanyDto dto) {
        entity.setCode(dto.getCode().trim().toUpperCase());
        entity.setName(dto.getName().trim());
        entity.setNameEn(dto.getNameEn() != null ? dto.getNameEn().trim() : null);
        entity.setWebsite(dto.getWebsite() != null ? dto.getWebsite().trim() : null);
        entity.setTrackingUrlTemplate(dto.getTrackingUrlTemplate() != null ? dto.getTrackingUrlTemplate().trim() : null);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        
        if (dto.getDisplayOrder() != null && dto.getDisplayOrder() >= 0) {
            entity.setDisplayOrder(dto.getDisplayOrder());
        }
    }

    /**
     * 택배사 DTO 검증
     */
    private void validateCourierCompanyDto(CourierCompanyDto dto) {
        if (!dto.hasRequiredFields()) {
            throw new IllegalArgumentException("택배사 코드와 이름은 필수입니다.");
        }
        
        if (!dto.isValidCode()) {
            throw new IllegalArgumentException("택배사 코드는 2-20자의 영문 대문자, 숫자, 밑줄(_)만 사용 가능합니다.");
        }
        
        if (dto.getWebsite() != null && !dto.getWebsite().trim().isEmpty() && !dto.isValidWebsite()) {
            throw new IllegalArgumentException("올바른 웹사이트 URL을 입력해주세요.");
        }
    }
}