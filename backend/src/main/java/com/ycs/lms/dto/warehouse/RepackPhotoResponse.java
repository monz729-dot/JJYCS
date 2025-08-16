package com.ycs.lms.dto.warehouse;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 리패킹 사진 업로드 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepackPhotoResponse {

    private Long photoId;
    private Long boxId;
    private String type;
    private String photoUrl;
    private String description;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
} 