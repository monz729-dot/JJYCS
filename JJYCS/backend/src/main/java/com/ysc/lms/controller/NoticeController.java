package com.ysc.lms.controller;

import com.ysc.lms.domain.Notice;
import com.ysc.lms.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Notice> notices = noticeService.getAllActiveNotices(pageable);
            
            response.put("success", true);
            response.put("data", notices.getContent());
            response.put("totalElements", notices.getTotalElements());
            response.put("totalPages", notices.getTotalPages());
            response.put("currentPage", page);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "공지사항 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getNoticeById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Notice notice = noticeService.getNoticeById(id);
            if (notice == null || !notice.getIsActive()) {
                response.put("success", false);
                response.put("message", "공지사항을 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("data", notice);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "공지사항 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createNotice(@RequestBody Notice notice) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Notice createdNotice = noticeService.createNotice(notice);
            response.put("success", true);
            response.put("data", createdNotice);
            response.put("message", "공지사항이 등록되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "공지사항 등록 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateNotice(
            @PathVariable Long id, 
            @RequestBody Notice notice) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Notice updatedNotice = noticeService.updateNotice(id, notice);
            response.put("success", true);
            response.put("data", updatedNotice);
            response.put("message", "공지사항이 수정되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "공지사항 수정 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteNotice(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            noticeService.deleteNotice(id);
            response.put("success", true);
            response.put("message", "공지사항이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "공지사항 삭제 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}