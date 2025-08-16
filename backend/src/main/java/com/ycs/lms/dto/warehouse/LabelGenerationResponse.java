package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 라벨 생성 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabelGenerationResponse {

    private Long boxId;
    private String labelCode;
    private String qrCodeUrl;
    private String barcodeUrl;
    private String pdfUrl;
    private String imageUrl;
    private String labelType;
    private String format;
} 