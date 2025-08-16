package com.ycs.lms.dto.orders;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 주문 취소 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancelOrderRequest {

    @NotNull(message = "취소 사유는 필수입니다.")
    @Size(max = 1000, message = "취소 사유는 1000자를 초과할 수 없습니다.")
    private String reason;
} 