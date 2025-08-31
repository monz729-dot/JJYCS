package com.ysc.lms.controller;

import com.ysc.lms.entity.User;
import com.ysc.lms.repository.UserRepository;
import com.ysc.lms.service.BulkRecipientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bulk/recipients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class BulkRecipientController {

    private final BulkRecipientService bulkRecipientService;
    private final UserRepository userRepository;

    /**
     * 수취인 정보 일괄 업로드 (CSV 파일)
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> uploadRecipients(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            log.info("수취인 일괄 업로드 요청: 사용자={}, 파일명={}", userEmail, file.getOriginalFilename());

            Map<String, Object> result = bulkRecipientService.uploadRecipients(user, file);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수취인 정보가 성공적으로 업로드되었습니다.",
                "data", result
            ));

        } catch (Exception e) {
            log.error("수취인 일괄 업로드 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "업로드 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 사용자별 등록된 수취인 목록 조회
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> getRecipientList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            Map<String, Object> result = bulkRecipientService.getRecipientList(user, page, size);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result
            ));

        } catch (Exception e) {
            log.error("수취인 목록 조회 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "목록 조회 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 수취인 정보 수정
     */
    @PutMapping("/{recipientId}")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> updateRecipient(
            @PathVariable Long recipientId,
            @RequestBody Map<String, Object> recipientData,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            Map<String, Object> result = bulkRecipientService.updateRecipient(user, recipientId, recipientData);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수취인 정보가 수정되었습니다.",
                "data", result
            ));

        } catch (Exception e) {
            log.error("수취인 정보 수정 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "수정 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 수취인 정보 삭제
     */
    @DeleteMapping("/{recipientId}")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> deleteRecipient(
            @PathVariable Long recipientId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            bulkRecipientService.deleteRecipient(user, recipientId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수취인 정보가 삭제되었습니다."
            ));

        } catch (Exception e) {
            log.error("수취인 정보 삭제 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "삭제 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * CSV 템플릿 다운로드
     */
    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> downloadTemplate() {
        try {
            byte[] templateData = bulkRecipientService.generateCsvTemplate();
            
            return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=recipient_template.csv")
                .body(templateData);

        } catch (Exception e) {
            log.error("템플릿 다운로드 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "템플릿 다운로드 실패: " + e.getMessage()
            ));
        }
    }
}