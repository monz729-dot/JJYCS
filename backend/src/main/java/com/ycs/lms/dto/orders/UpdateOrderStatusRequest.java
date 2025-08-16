package com.ycs.lms.dto.orders;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 주문 상태 변경 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "주문 상태는 필수입니다.")
    @Size(max = 20, message = "주문 상태는 20자를 초과할 수 없습니다.")
    private String status;

    @Size(max = 1000, message = "상태 변경 사유는 1000자를 초과할 수 없습니다.")
    private String reason;
} 