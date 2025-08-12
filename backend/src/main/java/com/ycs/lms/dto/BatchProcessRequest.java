package com.ycs.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchProcessRequest {
    
    @NotBlank(message = "작업 타입은 필수입니다")
    @Pattern(regexp = "^(inbound|outbound|hold|mixbox)$",
             message = "작업 타입은 inbound, outbound, hold, mixbox 중 하나여야 합니다")
    private String action;
    
    @NotEmpty(message = "라벨 코드 목록은 필수입니다")
    private List<String> labelCodes;
    
    @NotNull(message = "창고 ID는 필수입니다")
    private Long warehouseId;
    
    private String note;
    private String location; // 일괄 위치 지정
}