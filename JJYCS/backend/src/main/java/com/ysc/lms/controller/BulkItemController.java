package com.ysc.lms.controller;

import com.ysc.lms.entity.User;
import com.ysc.lms.repository.UserRepository;
import com.ysc.lms.service.BulkItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/bulk/items")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class BulkItemController {

    private final BulkItemService bulkItemService;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> uploadItems(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            log.info("품목 일괄 업로드 요청: 사용자={}, 파일명={}", userEmail, file.getOriginalFilename());

            Map<String, Object> result = bulkItemService.uploadItems(user, file);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "품목 정보가 성공적으로 업로드되었습니다.",
                "data", result
            ));

        } catch (Exception e) {
            log.error("품목 일괄 업로드 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "업로드 실패: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> getItemList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            Map<String, Object> result = bulkItemService.getItemList(user, page, size, category);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result
            ));

        } catch (Exception e) {
            log.error("품목 목록 조회 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "목록 조회 실패: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> searchItems(
            @RequestParam String keyword,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            Map<String, Object> result = bulkItemService.searchItems(user, keyword);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result
            ));

        } catch (Exception e) {
            log.error("품목 검색 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "검색 실패: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> updateItem(
            @PathVariable Long itemId,
            @RequestBody Map<String, Object> itemData,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            Map<String, Object> result = bulkItemService.updateItem(user, itemId, itemData);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "품목 정보가 수정되었습니다.",
                "data", result
            ));

        } catch (Exception e) {
            log.error("품목 정보 수정 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "수정 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 품목 정보 삭제
     */
    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> deleteItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            bulkItemService.deleteItem(user, itemId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "품목 정보가 삭제되었습니다."
            ));

        } catch (Exception e) {
            log.error("품목 정보 삭제 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "삭제 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 드롭다운용 활성 품목 목록
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> getActiveItems(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            Map<String, Object> result = bulkItemService.getActiveItems(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result
            ));

        } catch (Exception e) {
            log.error("활성 품목 조회 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "조회 실패: " + e.getMessage()
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
            byte[] templateData = bulkItemService.generateCsvTemplate();
            
            return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=item_template.csv")
                .body(templateData);

        } catch (Exception e) {
            log.error("템플릿 다운로드 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "템플릿 다운로드 실패: " + e.getMessage()
            ));
        }
    }

    /**
     * 통계 정보 조회 (카테고리별, HS코드별)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('CORPORATE', 'PARTNER')")
    public ResponseEntity<?> getItemStats(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            Map<String, Object> result = bulkItemService.getItemStats(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result
            ));

        } catch (Exception e) {
            log.error("품목 통계 조회 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "통계 조회 실패: " + e.getMessage()
            ));
        }
    }
}