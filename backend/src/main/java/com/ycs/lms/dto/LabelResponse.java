package com.ycs.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelResponse {
    private Long boxId;
    private String labelCode;
    private String qrCodeUrl;
    private String qrCodeData; // QR 코드 데이터 (JSON)
    private String labelImageUrl; // 라벨 이미지 URL
    private boolean printable;
    private String format; // PDF, PNG, etc.
}