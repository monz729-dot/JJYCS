package com.ysc.lms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 주문 품목 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    
    private Long id;
    
    /**
     * HS 코드 (필수)
     */
    @NotBlank(message = "HS 코드는 필수입니다.")
    @Pattern(regexp = "^[0-9]{4}\\.[0-9]{2}$", message = "HS 코드 형식이 올바르지 않습니다. (예: 6101.20)")
    @JsonProperty("hsCode")
    private String hsCode;
    
    /**
     * 품목 설명 (필수)
     */
    @NotBlank(message = "품목 설명은 필수입니다.")
    @Size(max = 500, message = "품목 설명은 500자 이하로 입력해주세요.")
    private String description;
    
    /**
     * 수량 (필수, 1 이상)
     */
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    @Max(value = 9999, message = "수량은 9999개 이하여야 합니다.")
    private Integer quantity;
    
    /**
     * 무게 (kg, 필수)
     */
    @NotNull(message = "무게는 필수입니다.")
    @DecimalMin(value = "0.1", message = "무게는 0.1kg 이상이어야 합니다.")
    @DecimalMax(value = "999.99", message = "무게는 999.99kg 이하여야 합니다.")
    @Digits(integer = 3, fraction = 2, message = "무게는 소수점 2자리까지 입력 가능합니다.")
    private BigDecimal weight;
    
    /**
     * 가로 (cm, 필수)
     */
    @NotNull(message = "가로 크기는 필수입니다.")
    @DecimalMin(value = "1.0", message = "가로는 1cm 이상이어야 합니다.")
    @DecimalMax(value = "999.9", message = "가로는 999.9cm 이하여야 합니다.")
    @Digits(integer = 3, fraction = 1, message = "가로는 소수점 1자리까지 입력 가능합니다.")
    private BigDecimal width;
    
    /**
     * 세로 (cm, 필수)
     */
    @NotNull(message = "세로 크기는 필수입니다.")
    @DecimalMin(value = "1.0", message = "세로는 1cm 이상이어야 합니다.")
    @DecimalMax(value = "999.9", message = "세로는 999.9cm 이하여야 합니다.")
    @Digits(integer = 3, fraction = 1, message = "세로는 소수점 1자리까지 입력 가능합니다.")
    private BigDecimal height;
    
    /**
     * 높이 (cm, 필수)
     */
    @NotNull(message = "높이는 필수입니다.")
    @DecimalMin(value = "1.0", message = "높이는 1cm 이상이어야 합니다.")
    @DecimalMax(value = "999.9", message = "높이는 999.9cm 이하여야 합니다.")
    @Digits(integer = 3, fraction = 1, message = "높이는 소수점 1자리까지 입력 가능합니다.")
    private BigDecimal depth;
    
    /**
     * 단가 (THB, 선택사항)
     */
    @DecimalMin(value = "0.01", message = "단가는 0.01THB 이상이어야 합니다.")
    @DecimalMax(value = "999999.99", message = "단가는 999999.99THB 이하여야 합니다.")
    @Digits(integer = 6, fraction = 2, message = "단가는 소수점 2자리까지 입력 가능합니다.")
    @JsonProperty("unitPrice")
    private BigDecimal unitPrice;
    
    /**
     * 총 가격 (THB, 계산값 또는 직접 입력)
     */
    @DecimalMin(value = "0.01", message = "총 가격은 0.01THB 이상이어야 합니다.")
    @DecimalMax(value = "9999999.99", message = "총 가격은 9999999.99THB 이하여야 합니다.")
    @Digits(integer = 7, fraction = 2, message = "총 가격은 소수점 2자리까지 입력 가능합니다.")
    @JsonProperty("totalPrice")
    private BigDecimal totalPrice;
    
    // === 계산 메서드들 ===
    
    /**
     * CBM 계산 (m³)
     * CBM = (가로 × 세로 × 높이) / 1,000,000
     */
    public BigDecimal getCbm() {
        if (width == null || height == null || depth == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal volume = width.multiply(height).multiply(depth);
        BigDecimal cbm = volume.divide(new BigDecimal("1000000"), 6, RoundingMode.HALF_UP);
        return cbm;
    }
    
    /**
     * 총 CBM 계산 (수량 포함)
     */
    public BigDecimal getTotalCbm() {
        if (quantity == null) {
            return getCbm();
        }
        return getCbm().multiply(new BigDecimal(quantity));
    }
    
    /**
     * 총 무게 계산 (수량 포함)
     */
    public BigDecimal getTotalWeight() {
        if (weight == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return weight.multiply(new BigDecimal(quantity));
    }
    
    /**
     * 총 가격 자동 계산 (단가 × 수량)
     */
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
    
    /**
     * 단가 자동 계산 (총 가격 ÷ 수량)
     */
    public void calculateUnitPrice() {
        if (totalPrice != null && quantity != null && quantity > 0) {
            this.unitPrice = totalPrice.divide(new BigDecimal(quantity), 2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * THB 1,500 초과 여부 (세관 신고 임계값)
     */
    public boolean exceedsCustomsThreshold() {
        BigDecimal threshold = new BigDecimal("1500.00");
        return totalPrice != null && totalPrice.compareTo(threshold) > 0;
    }
    
    /**
     * HS 코드 검증
     */
    public boolean isValidHsCode() {
        return hsCode != null && hsCode.matches("^[0-9]{4}\\.[0-9]{2}$");
    }
    
    /**
     * 필수 치수 정보 완료 여부
     */
    public boolean hasDimensionInfo() {
        return width != null && width.compareTo(BigDecimal.ZERO) > 0 &&
               height != null && height.compareTo(BigDecimal.ZERO) > 0 &&
               depth != null && depth.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 품목 요약 정보
     */
    public String getSummary() {
        return String.format("%s (HS:%s) %d개 - %.3fm³", 
            description != null ? description.substring(0, Math.min(description.length(), 20)) : "품목명없음",
            hsCode != null ? hsCode : "미입력",
            quantity != null ? quantity : 0,
            getTotalCbm().doubleValue()
        );
    }
    
    /**
     * 생성자 - 필수 필드만
     */
    public OrderItemDto(String hsCode, String description, Integer quantity) {
        this.hsCode = hsCode;
        this.description = description;
        this.quantity = quantity;
    }
}