package com.ysc.lms.service;

import com.ysc.lms.entity.Parcel;
import com.ysc.lms.repository.ParcelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 택배 추적 및 송장번호 유효성 검증 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParcelTrackingService {
    
    private final ParcelRepository parcelRepository;
    private final RestTemplate restTemplate;
    private final CarrierApiService carrierApiService;
    
    // 지원하는 택배사 코드 매핑
    private static final Map<String, String> CARRIER_CODES = Map.of(
        "CJ", "CJ대한통운",
        "LOTTE", "롯데택배", 
        "HANJIN", "한진택배",
        "POST", "우체국택배",
        "KDEXP", "경동택배",
        "CJGLS", "CJ물류",
        "DAESIN", "대신택배",
        "ILYANG", "일양로지스"
    );
    
    /**
     * 송장번호 유효성 즉시 검증
     */
    @Transactional
    public ValidationResult validateTrackingNumber(String carrier, String trackingNumber) {
        try {
            log.info("Validating tracking number: {} for carrier: {}", trackingNumber, carrier);
            
            // 기본 형식 검증
            if (!isValidFormat(carrier, trackingNumber)) {
                return ValidationResult.invalid("송장번호 형식이 올바르지 않습니다.");
            }
            
            // 실제 택배 API 호출 (현재는 목업 응답)
            TrackingInfo trackingInfo = callCarrierAPI(carrier, trackingNumber);
            
            if (trackingInfo.isValid()) {
                return ValidationResult.valid(trackingInfo.getStatus(), trackingInfo.getDescription());
            } else {
                return ValidationResult.invalid(trackingInfo.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("Error validating tracking number: {} for carrier: {}", trackingNumber, carrier, e);
            return ValidationResult.error("검증 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 택배 상태 업데이트 (배치 작업용)
     */
    @Transactional
    public void updateParcelStatuses() {
        log.info("Starting batch update of parcel statuses");
        
        // PENDING 상태이거나 1시간 이상 업데이트되지 않은 택배들
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        List<Parcel> parcelsToUpdate = parcelRepository.findPendingValidationParcels(cutoffTime);
        
        for (Parcel parcel : parcelsToUpdate) {
            try {
                ValidationResult result = validateTrackingNumber(parcel.getCarrier(), parcel.getTrackingNumber());
                updateParcelValidationStatus(parcel, result);
            } catch (Exception e) {
                log.error("Failed to update parcel status for tracking: {}", parcel.getTrackingNumber(), e);
            }
        }
        
        log.info("Completed batch update of {} parcels", parcelsToUpdate.size());
    }
    
    /**
     * 입고 스캔과 주문 매칭
     */
    @Transactional
    public MatchingResult matchInboundScan(String trackingNumber) {
        try {
            Optional<Parcel> parcelOpt = parcelRepository.findByTrackingNumber(trackingNumber);
            
            if (parcelOpt.isEmpty()) {
                // 사전 입력된 송장이 없는 경우 - 수기 검색 필요
                return MatchingResult.needsManualSearch("해당 송장번호와 매칭된 주문이 없습니다.");
            }
            
            Parcel parcel = parcelOpt.get();
            
            if (parcel.getIsMatched()) {
                return MatchingResult.alreadyMatched("이미 매칭된 송장입니다.");
            }
            
            // 자동 매칭 성공
            parcel.markAsMatched("스캔을 통한 자동 매칭");
            parcelRepository.save(parcel);
            
            return MatchingResult.success(parcel.getOrder(), "자동 매칭 완료");
            
        } catch (Exception e) {
            log.error("Error matching inbound scan for tracking: {}", trackingNumber, e);
            return MatchingResult.error("매칭 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 수동 매칭 검색
     */
    public List<Parcel> searchForManualMatching(String keyword) {
        List<Parcel> results = new ArrayList<>();
        
        // 수취인/연락처로 검색
        results.addAll(parcelRepository.findUnmatchedByRecipientKeyword(keyword));
        
        // 품목명으로 검색
        results.addAll(parcelRepository.findUnmatchedByItemKeyword(keyword));
        
        return results.stream().distinct().toList();
    }
    
    /**
     * 수동 매칭 처리
     */
    @Transactional
    public void manualMatch(Long parcelId, String notes) {
        Parcel parcel = parcelRepository.findById(parcelId)
            .orElseThrow(() -> new IllegalArgumentException("Parcel not found"));
        
        parcel.markAsMatched("수동 매칭: " + notes);
        parcelRepository.save(parcel);
        
        log.info("Manual matching completed for parcel: {}", parcelId);
    }
    
    private void updateParcelValidationStatus(Parcel parcel, ValidationResult result) {
        parcel.updateValidationStatus(result.getStatus(), result.getMessage());
        parcel.setLastKnownStatus(result.getTrackingStatus());
        parcelRepository.save(parcel);
    }
    
    private boolean isValidFormat(String carrier, String trackingNumber) {
        // 택배사별 송장번호 형식 검증
        return switch (carrier.toUpperCase()) {
            case "CJ" -> trackingNumber.matches("\\d{10,13}");
            case "LOTTE" -> trackingNumber.matches("\\d{12,13}");
            case "HANJIN" -> trackingNumber.matches("\\d{10,12}");
            case "POST" -> trackingNumber.matches("\\d{13}");
            default -> trackingNumber.matches("\\d{8,15}"); // 기본 패턴
        };
    }
    
    private TrackingInfo callCarrierAPI(String carrier, String trackingNumber) {
        try {
            log.info("Calling real carrier API for {}: {}", carrier, trackingNumber);
            
            CarrierApiService.TrackingResult result = carrierApiService.getTrackingInfo(carrier, trackingNumber);
            
            if (result.isSuccess()) {
                return TrackingInfo.valid(result.getStatus(), result.getLocation() + " - " + result.getTime());
            } else {
                return TrackingInfo.invalid(result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("Carrier API call failed for {}: {}", carrier, trackingNumber, e);
            
            // 폴백: 기본 형식 검증만 수행
            log.warn("Fallback to basic validation for {}: {}", carrier, trackingNumber);
            return TrackingInfo.valid("접수", "기본 검증 통과 (택배사 서비스 일시 불가)");
        }
    }
    
    // 내부 클래스들
    
    public static class ValidationResult {
        private final Parcel.ValidationStatus status;
        private final String message;
        private final String trackingStatus;
        
        private ValidationResult(Parcel.ValidationStatus status, String message, String trackingStatus) {
            this.status = status;
            this.message = message;
            this.trackingStatus = trackingStatus;
        }
        
        public static ValidationResult valid(String trackingStatus, String description) {
            return new ValidationResult(Parcel.ValidationStatus.VALID, 
                "유효한 송장번호입니다. " + description, trackingStatus);
        }
        
        public static ValidationResult invalid(String message) {
            return new ValidationResult(Parcel.ValidationStatus.INVALID, message, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(Parcel.ValidationStatus.ERROR, message, null);
        }
        
        // Getters
        public Parcel.ValidationStatus getStatus() { return status; }
        public String getMessage() { return message; }
        public String getTrackingStatus() { return trackingStatus; }
        public boolean isValid() { return status == Parcel.ValidationStatus.VALID; }
    }
    
    public static class TrackingInfo {
        private final boolean valid;
        private final String status;
        private final String description;
        private final String errorMessage;
        
        private TrackingInfo(boolean valid, String status, String description, String errorMessage) {
            this.valid = valid;
            this.status = status;
            this.description = description;
            this.errorMessage = errorMessage;
        }
        
        public static TrackingInfo valid(String status, String description) {
            return new TrackingInfo(true, status, description, null);
        }
        
        public static TrackingInfo invalid(String errorMessage) {
            return new TrackingInfo(false, null, null, errorMessage);
        }
        
        public static TrackingInfo error(String errorMessage) {
            return new TrackingInfo(false, null, null, errorMessage);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getStatus() { return status; }
        public String getDescription() { return description; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public static class MatchingResult {
        private final boolean success;
        private final String message;
        private final com.ysc.lms.entity.Order order;
        private final boolean needsManualSearch;
        private final boolean alreadyMatched;
        
        private MatchingResult(boolean success, String message, com.ysc.lms.entity.Order order, 
                              boolean needsManualSearch, boolean alreadyMatched) {
            this.success = success;
            this.message = message;
            this.order = order;
            this.needsManualSearch = needsManualSearch;
            this.alreadyMatched = alreadyMatched;
        }
        
        public static MatchingResult success(com.ysc.lms.entity.Order order, String message) {
            return new MatchingResult(true, message, order, false, false);
        }
        
        public static MatchingResult needsManualSearch(String message) {
            return new MatchingResult(false, message, null, true, false);
        }
        
        public static MatchingResult alreadyMatched(String message) {
            return new MatchingResult(false, message, null, false, true);
        }
        
        public static MatchingResult error(String message) {
            return new MatchingResult(false, message, null, false, false);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public com.ysc.lms.entity.Order getOrder() { return order; }
        public boolean isNeedsManualSearch() { return needsManualSearch; }
        public boolean isAlreadyMatched() { return alreadyMatched; }
    }
}