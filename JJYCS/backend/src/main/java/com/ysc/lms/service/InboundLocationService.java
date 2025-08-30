package com.ysc.lms.service;

import com.ysc.lms.dto.InboundLocationDto;
import com.ysc.lms.entity.InboundLocation;
import com.ysc.lms.repository.InboundLocationRepository;
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
 * YCS 접수지 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InboundLocationService {
    
    private final InboundLocationRepository inboundLocationRepository;

    /**
     * 접수지 목록 조회 (페이징, 검색)
     */
    public Page<InboundLocationDto> getInboundLocations(Boolean isActive, String search, Pageable pageable) {
        Page<InboundLocation> locations;
        
        if (search != null && !search.trim().isEmpty()) {
            if (isActive != null) {
                locations = inboundLocationRepository.findByIsActiveAndNameContainingOrAddressContainingOrderByDisplayOrder(
                    isActive, search.trim(), search.trim(), pageable);
            } else {
                locations = inboundLocationRepository.findByNameContainingOrAddressContainingOrderByDisplayOrder(
                    search.trim(), search.trim(), pageable);
            }
        } else {
            if (isActive != null) {
                locations = inboundLocationRepository.findByIsActiveOrderByDisplayOrder(isActive, pageable);
            } else {
                locations = inboundLocationRepository.findAllByOrderByDisplayOrder(pageable);
            }
        }
        
        return locations.map(this::convertToDto);
    }

    /**
     * 활성 접수지 목록 조회 (표시 순서별)
     */
    public List<InboundLocationDto> getActiveInboundLocations() {
        List<InboundLocation> locations = inboundLocationRepository.findByIsActiveTrueOrderByDisplayOrder();
        return locations.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * 접수지 상세 조회
     */
    public InboundLocationDto getInboundLocationById(Long id) {
        InboundLocation location = inboundLocationRepository.findById(id).orElse(null);
        return location != null ? convertToDto(location) : null;
    }

    /**
     * 새 접수지 등록
     */
    @Transactional
    public InboundLocationDto createInboundLocation(InboundLocationDto locationDto) {
        // 입력 검증
        validateInboundLocationDto(locationDto);
        
        // 중복 이름 검증
        if (inboundLocationRepository.existsByName(locationDto.getName().trim())) {
            throw new IllegalArgumentException("이미 동일한 이름의 접수지가 존재합니다.");
        }
        
        InboundLocation location = new InboundLocation();
        updateLocationFromDto(location, locationDto);
        
        // 표시 순서 설정 (기존 최대값 + 1)
        if (locationDto.getDisplayOrder() == null || locationDto.getDisplayOrder() == 0) {
            Integer maxOrder = inboundLocationRepository.findMaxDisplayOrder();
            location.setDisplayOrder(maxOrder != null ? maxOrder + 1 : 1);
        }
        
        location.setCreatedAt(LocalDateTime.now());
        location.setUpdatedAt(LocalDateTime.now());
        
        InboundLocation saved = inboundLocationRepository.save(location);
        
        log.info("Created new inbound location: {} (ID: {})", saved.getName(), saved.getId());
        
        return convertToDto(saved);
    }

    /**
     * 접수지 정보 수정
     */
    @Transactional
    public InboundLocationDto updateInboundLocation(Long id, InboundLocationDto locationDto) {
        InboundLocation location = inboundLocationRepository.findById(id).orElse(null);
        
        if (location == null) {
            return null;
        }
        
        // 입력 검증
        validateInboundLocationDto(locationDto);
        
        // 중복 이름 검증 (본인 제외)
        if (!location.getName().equals(locationDto.getName().trim()) &&
            inboundLocationRepository.existsByName(locationDto.getName().trim())) {
            throw new IllegalArgumentException("이미 동일한 이름의 접수지가 존재합니다.");
        }
        
        updateLocationFromDto(location, locationDto);
        location.setUpdatedAt(LocalDateTime.now());
        
        InboundLocation saved = inboundLocationRepository.save(location);
        
        log.info("Updated inbound location: {} (ID: {})", saved.getName(), saved.getId());
        
        return convertToDto(saved);
    }

    /**
     * 접수지 활성/비활성 토글
     */
    @Transactional
    public InboundLocationDto toggleInboundLocationStatus(Long id) {
        InboundLocation location = inboundLocationRepository.findById(id).orElse(null);
        
        if (location == null) {
            return null;
        }
        
        location.setIsActive(!location.getIsActive());
        location.setUpdatedAt(LocalDateTime.now());
        
        InboundLocation saved = inboundLocationRepository.save(location);
        
        log.info("Toggled inbound location status: {} -> {} (ID: {})", 
            saved.getName(), saved.getIsActive() ? "ACTIVE" : "INACTIVE", saved.getId());
        
        return convertToDto(saved);
    }

    /**
     * 접수지 표시 순서 변경
     */
    @Transactional
    public InboundLocationDto updateDisplayOrder(Long id, Integer displayOrder) {
        InboundLocation location = inboundLocationRepository.findById(id).orElse(null);
        
        if (location == null) {
            return null;
        }
        
        if (displayOrder == null || displayOrder < 0) {
            throw new IllegalArgumentException("표시 순서는 0 이상의 숫자여야 합니다.");
        }
        
        location.setDisplayOrder(displayOrder);
        location.setUpdatedAt(LocalDateTime.now());
        
        InboundLocation saved = inboundLocationRepository.save(location);
        
        log.info("Updated display order for inbound location: {} -> {} (ID: {})", 
            saved.getName(), displayOrder, saved.getId());
        
        return convertToDto(saved);
    }

    /**
     * 접수지 삭제 (실제로는 비활성화)
     */
    @Transactional
    public boolean deleteInboundLocation(Long id) {
        InboundLocation location = inboundLocationRepository.findById(id).orElse(null);
        
        if (location == null) {
            return false;
        }
        
        // 사용 중인 접수지인지 확인 (주문에서 참조하고 있는지)
        long orderCount = inboundLocationRepository.countOrdersUsingLocation(id);
        
        if (orderCount > 0) {
            throw new IllegalStateException(
                String.format("해당 접수지는 %d개의 주문에서 사용 중이므로 삭제할 수 없습니다. 비활성화를 권장합니다.", orderCount));
        }
        
        // 실제 삭제 대신 비활성화
        location.setIsActive(false);
        location.setUpdatedAt(LocalDateTime.now());
        
        inboundLocationRepository.save(location);
        
        log.info("Deactivated inbound location: {} (ID: {})", location.getName(), location.getId());
        
        return true;
    }
    
    // === 내부 메서드들 ===

    /**
     * Entity -> DTO 변환
     */
    private InboundLocationDto convertToDto(InboundLocation entity) {
        return InboundLocationDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .address(entity.getAddress())
            .postalCode(entity.getPostalCode())
            .phone(entity.getPhone())
            .contactPerson(entity.getContactPerson())
            .businessHours(entity.getBusinessHours())
            .specialInstructions(entity.getSpecialInstructions())
            .isActive(entity.getIsActive())
            .displayOrder(entity.getDisplayOrder())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * DTO -> Entity 필드 복사
     */
    private void updateLocationFromDto(InboundLocation entity, InboundLocationDto dto) {
        entity.setName(dto.getName().trim());
        entity.setAddress(dto.getAddress().trim());
        entity.setPostalCode(dto.getPostalCode() != null ? dto.getPostalCode().trim() : null);
        entity.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        entity.setContactPerson(dto.getContactPerson() != null ? dto.getContactPerson().trim() : null);
        entity.setBusinessHours(dto.getBusinessHours() != null ? dto.getBusinessHours().trim() : null);
        entity.setSpecialInstructions(dto.getSpecialInstructions() != null ? dto.getSpecialInstructions().trim() : null);
        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        
        if (dto.getDisplayOrder() != null && dto.getDisplayOrder() >= 0) {
            entity.setDisplayOrder(dto.getDisplayOrder());
        }
    }

    /**
     * 접수지 DTO 검증
     */
    private void validateInboundLocationDto(InboundLocationDto dto) {
        if (!dto.hasRequiredFields()) {
            throw new IllegalArgumentException("접수지명과 주소는 필수입니다.");
        }
        
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty() && !dto.isValidKoreanPhone()) {
            throw new IllegalArgumentException("한국 전화번호 형식이 올바르지 않습니다. (예: 02-1234-5678)");
        }
        
        if (dto.getPostalCode() != null && !dto.getPostalCode().trim().isEmpty() && !dto.isValidKoreanPostalCode()) {
            throw new IllegalArgumentException("우편번호는 5자리 숫자입니다.");
        }
    }
}