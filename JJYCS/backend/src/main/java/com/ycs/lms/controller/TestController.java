package com.ycs.lms.controller;

import com.ycs.lms.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    
    private final EmailService emailService;
    
    @GetMapping("/password")
    public Map<String, String> generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password123";
        String encodedPassword = encoder.encode(rawPassword);
        return Map.of(
            "raw", rawPassword,
            "encoded", encodedPassword
        );
    }
    
    @PostMapping("/email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            emailService.sendTestEmail(email);
            
            response.put("success", true);
            response.put("message", "테스트 이메일이 성공적으로 발송되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/verification-email")
    public ResponseEntity<Map<String, Object>> testVerificationEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            String name = request.get("name");
            String token = UUID.randomUUID().toString();
            
            emailService.sendVerificationEmail(email, name, token);
            
            response.put("success", true);
            response.put("message", "인증 이메일이 성공적으로 발송되었습니다.");
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/password-reset-email")
    public ResponseEntity<Map<String, Object>> testPasswordResetEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            String name = request.get("name");
            String token = UUID.randomUUID().toString();
            
            emailService.sendPasswordResetEmail(email, name, token);
            
            response.put("success", true);
            response.put("message", "비밀번호 재설정 이메일이 성공적으로 발송되었습니다.");
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}