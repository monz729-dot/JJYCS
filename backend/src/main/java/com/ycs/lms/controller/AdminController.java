package com.ycs.lms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @GetMapping("/users/pending")
    public ResponseEntity<?> getPendingUsers() {
        // Get pending users from AuthController's storage
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", getPendingRegistrations(),
            "message", "승인 대기 중인 사용자 목록을 조회했습니다."
        ));
    }
    
    @PostMapping("/users/{userId}/approve")
    public ResponseEntity<?> approveUser(@PathVariable String userId, @RequestBody Map<String, String> request) {
        try {
            // Simulate approval process
            Thread.sleep(1000); // Simulate processing delay
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "사용자 승인이 완료되었습니다.");
            response.put("userId", userId);
            response.put("approvedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("approvedBy", "admin@ycs.com");
            
            // In real implementation, this would update the database
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "승인 처리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/users/{userId}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable String userId, @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "거절 사유를 입력해주세요."
                ));
            }
            
            // Simulate rejection process
            Thread.sleep(1000);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "사용자 가입이 거절되었습니다.");
            response.put("userId", userId);
            response.put("rejectedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("rejectedBy", "admin@ycs.com");
            response.put("reason", reason);
            
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "거절 처리 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/stats/registrations")
    public ResponseEntity<?> getRegistrationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 15);
        stats.put("pending", 3);
        stats.put("approved", 10);
        stats.put("rejected", 2);
        stats.put("today", 5);
        stats.put("thisWeek", 12);
        stats.put("thisMonth", 15);
        
        Map<String, Object> roleBreakdown = new HashMap<>();
        roleBreakdown.put("individual", 8);
        roleBreakdown.put("enterprise", 4);
        roleBreakdown.put("partner", 2);
        roleBreakdown.put("warehouse", 1);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "stats", stats,
            "roleBreakdown", roleBreakdown,
            "averageApprovalTime", "1.2일"
        ));
    }
    
    // Mock data for demonstration
    private List<Map<String, Object>> getPendingRegistrations() {
        List<Map<String, Object>> users = new ArrayList<>();
        
        // Sample pending enterprise user
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "user_001");
        user1.put("name", "김기업");
        user1.put("email", "kim.enterprise@company.com");
        user1.put("phone", "010-1234-5678");
        user1.put("role", "enterprise");
        user1.put("approvalStatus", "pending");
        user1.put("registeredAt", "2025-08-12T10:30:00");
        
        Map<String, Object> enterpriseProfile = new HashMap<>();
        enterpriseProfile.put("companyName", "(주)글로벌무역");
        enterpriseProfile.put("businessNumber", "123-45-67890");
        enterpriseProfile.put("businessAddress", "서울시 강남구 테헤란로 123");
        user1.put("enterpriseProfile", enterpriseProfile);
        
        // Sample pending partner user
        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "user_002");
        user2.put("name", "박파트너");
        user2.put("email", "park.partner@affiliate.com");
        user2.put("phone", "010-9876-5432");
        user2.put("role", "partner");
        user2.put("approvalStatus", "pending");
        user2.put("registeredAt", "2025-08-12T09:15:00");
        
        Map<String, Object> partnerProfile = new HashMap<>();
        partnerProfile.put("partnerType", "affiliate");
        partnerProfile.put("businessLicense", "BL-2025-0001");
        user2.put("partnerProfile", partnerProfile);
        
        // Sample approved warehouse user
        Map<String, Object> user3 = new HashMap<>();
        user3.put("id", "user_003");
        user3.put("name", "최창고");
        user3.put("email", "choi.warehouse@logistics.com");
        user3.put("phone", "010-5555-6666");
        user3.put("role", "warehouse");
        user3.put("approvalStatus", "approved");
        user3.put("registeredAt", "2025-08-11T14:20:00");
        user3.put("approvedAt", "2025-08-12T08:30:00");
        user3.put("approvedBy", "admin@ycs.com");
        
        Map<String, Object> warehouseProfile = new HashMap<>();
        warehouseProfile.put("warehouseName", "YCS 방콕 창고");
        warehouseProfile.put("location", "방콕 라차다피세크");
        warehouseProfile.put("capacity", 1000);
        user3.put("warehouseProfile", warehouseProfile);
        
        users.add(user1);
        users.add(user2);
        users.add(user3);
        
        return users;
    }
}