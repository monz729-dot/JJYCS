package com.ycs.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanRequest {
    
    @NotBlank(message = "라벨 코드는 필수입니다")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "올바른 라벨 코드 형식이 아닙니다")
    private String labelCode;
    
    @NotBlank(message = "스캔 타입은 필수입니다")
    @Pattern(regexp = "^(inbound|outbound|hold|mixbox|inventory)$", 
             message = "스캔 타입은 inbound, outbound, hold, mixbox, inventory 중 하나여야 합니다")
    private String scanType;
    
    @NotNull(message = "창고 ID는 필수입니다")
    private Long warehouseId;
    
    @Size(max = 100, message = "위치 코드는 100자 이하여야 합니다")
    private String location;
    
    @Size(max = 1000, message = "메모는 1000자 이하여야 합니다")
    private String note;
    
    private List<PhotoInfo> photos;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoInfo {
        @NotBlank(message = "사진 타입은 필수입니다")
        @Pattern(regexp = "^(before|after|damage|label)$", 
                 message = "사진 타입은 before, after, damage, label 중 하나여야 합니다")
        private String type;
        
        @NotBlank(message = "사진 URL은 필수입니다")
        private String url;
        
        private String timestamp;
        private Long fileSize;
        private String mimeType;
    }
}