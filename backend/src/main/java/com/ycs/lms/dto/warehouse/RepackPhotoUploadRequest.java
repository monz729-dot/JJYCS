package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 리패킹 사진 업로드 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepackPhotoUploadRequest {

    @NotNull(message = "박스 ID는 필수입니다.")
    private Long boxId;

    @NotNull(message = "사진 타입은 필수입니다.")
    private String type; // "before", "after"

    @NotNull(message = "사진 데이터는 필수입니다.")
    private String photoData; // Base64 encoded image

    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다.")
    private String description;
} 