package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 일괄 처리 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchProcessRequest {

    @NotNull(message = "처리 액션은 필수입니다.")
    private BatchAction action;

    @NotEmpty(message = "라벨 코드 목록은 최소 1개 이상이어야 합니다.")
    private List<String> labelCodes;

    @NotNull(message = "창고 ID는 필수입니다.")
    private Long warehouseId;

    /**
     * 일괄 처리 액션 열거형
     */
    public enum BatchAction {
        INBOUND("입고"),
        OUTBOUND("출고"),
        HOLD("보류");

        private final String displayName;

        BatchAction(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 