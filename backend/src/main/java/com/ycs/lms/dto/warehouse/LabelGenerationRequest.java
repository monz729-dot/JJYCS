package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 라벨 생성 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabelGenerationRequest {

    @NotNull(message = "박스 ID는 필수입니다.")
    private Long boxId;

    private String labelType; // "qr", "barcode", "both"
    private String format; // "pdf", "image"
} 