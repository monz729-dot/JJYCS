package com.ycs.lms.controller;

import com.ycs.lms.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Profile("dev")
@RestController
@RequestMapping("/auth/dev")
@RequiredArgsConstructor
@Slf4j
public class MailDebugController {

    private final EmailService emailService;

    @GetMapping("/send-test")
    public ResponseEntity<?> sendTest(@RequestParam String to) {
        try {
            log.info("테스트 메일 발송 시작: {}", to);
            emailService.sendVerificationEmail(to, "테스트", "123456");
            log.info("테스트 메일 발송 완료: {}", to);
            return ResponseEntity.ok(Map.of("success", true, "message", "테스트 메일 발송 완료", "to", to));
        } catch (Exception e) {
            log.error("테스트 메일 발송 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    @PostMapping("/send-simple-test")
    public ResponseEntity<?> sendSimpleTest(@RequestBody Map<String, String> request) {
        String to = request.get("email");
        try {
            log.info("간단 테스트 메일 발송 시작: {}", to);
            emailService.sendSimpleEmail(to, "[YCS] 메일 연동 테스트", "메일 연동이 정상적으로 작동합니다!");
            log.info("간단 테스트 메일 발송 완료: {}", to);
            return ResponseEntity.ok(Map.of("success", true, "message", "간단 테스트 메일 발송 완료", "to", to));
        } catch (Exception e) {
            log.error("간단 테스트 메일 발송 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}