package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 스캔 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScanRequest {

    @NotNull(message = "라벨 코드는 필수입니다.")
    @Size(max = 50, message = "라벨 코드는 50자를 초과할 수 없습니다.")
    private String labelCode;

    @NotNull(message = "스캔 타입은 필수입니다.")
    private ScanType scanType;

    @Size(max = 1000, message = "메모는 1000자를 초과할 수 없습니다.")
    private String memo;

    /**
     * 스캔 타입 열거형
     */
    public enum ScanType {
        INBOUND("입고"),
        OUTBOUND("출고"),
        HOLD("보류"),
        MIXBOX("믹스박스");

        private final String displayName;

        ScanType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 