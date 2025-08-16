package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 라벨 출력 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrintLabelsRequest {

    @NotEmpty(message = "박스 ID 목록은 최소 1개 이상이어야 합니다.")
    private List<Long> boxIds;

    @NotNull(message = "출력 형식은 필수입니다.")
    private String format; // "pdf", "image"

    private String labelType; // "qr", "barcode", "both"
    private String paperSize; // "a4", "label"
} 