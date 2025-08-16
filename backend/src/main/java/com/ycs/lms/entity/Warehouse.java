package com.ycs.lms.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {
    private Long id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private String managerName;
    private String managerPhone;
    private String managerEmail;
    private WarehouseStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum WarehouseStatus {
        ACTIVE,       // 활성
        INACTIVE,     // 비활성
        MAINTENANCE   // 점검중
    }
}