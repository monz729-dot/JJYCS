package com.ysc.lms.controller;

import com.ysc.lms.domain.Faq;
import com.ysc.lms.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {
    private final FaqService faqService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFaqs(
            @RequestParam(required = false) String category) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Faq> faqs = category != null ? 
                faqService.getFaqsByCategory(category) : 
                faqService.getAllActiveFaqs();
                
            response.put("success", true);
            response.put("data", faqs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "FAQ 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFaqById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Faq faq = faqService.getFaqById(id);
            if (faq == null) {
                response.put("success", false);
                response.put("message", "FAQ를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("data", faq);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "FAQ 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createFaq(@RequestBody Faq faq) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Faq createdFaq = faqService.createFaq(faq);
            response.put("success", true);
            response.put("data", createdFaq);
            response.put("message", "FAQ가 생성되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "FAQ 생성 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateFaq(
            @PathVariable Long id, 
            @RequestBody Faq faq) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Faq updatedFaq = faqService.updateFaq(id, faq);
            response.put("success", true);
            response.put("data", updatedFaq);
            response.put("message", "FAQ가 수정되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "FAQ 수정 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteFaq(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            faqService.deleteFaq(id);
            response.put("success", true);
            response.put("message", "FAQ가 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "FAQ 삭제 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}