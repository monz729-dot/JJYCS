package com.ycs.lms.dto.orders;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 비즈니스 룰 검증 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessRuleValidationRequest {

    @Valid
    @NotEmpty(message = "박스 정보는 최소 1개 이상이어야 합니다.")
    private List<CBMCalculationRequest.Box> boxes;

    @NotNull(message = "통화는 필수입니다.")
    private String currency;

    @NotNull(message = "총 금액은 필수입니다.")
    private BigDecimal totalAmount;
} 