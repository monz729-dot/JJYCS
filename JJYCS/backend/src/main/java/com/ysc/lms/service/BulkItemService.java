package com.ysc.lms.service;

import com.ysc.lms.entity.BulkItem;
import com.ysc.lms.entity.User;
import com.ysc.lms.repository.BulkItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BulkItemService {

    private final BulkItemRepository bulkItemRepository;

    /**
     * CSV 파일로 품목 정보 일괄 업로드
     */
    public Map<String, Object> uploadItems(User user, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("CSV 파일만 업로드 가능합니다.");
        }

        List<BulkItem> items = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalRows = 0;
        int successCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            int rowNumber = 1;
            
            // 헤더 라인 스킵
            if ((line = reader.readLine()) != null) {
                totalRows++;
            }

            while ((line = reader.readLine()) != null) {
                rowNumber++;
                totalRows++;

                try {
                    String[] columns = parseCsvLine(line);
                    if (columns.length < 3) {
                        errors.add("라인 " + rowNumber + ": 필수 컬럼이 부족합니다. (HS코드, 품목명, 영문명)");
                        continue;
                    }

                    // 필수 필드 검증
                    String hsCode = columns[0].trim();
                    String description = columns[1].trim();
                    String englishName = columns[2].trim();

                    if (hsCode.isEmpty() || description.isEmpty() || englishName.isEmpty()) {
                        errors.add("라인 " + rowNumber + ": 필수 필드가 비어있습니다.");
                        continue;
                    }

                    // HS 코드 형식 검증 (기본적인 검증)
                    if (!hsCode.matches("\\d{4,10}")) {
                        errors.add("라인 " + rowNumber + ": HS 코드 형식이 올바르지 않습니다. (" + hsCode + ")");
                        continue;
                    }

                    // 중복 체크
                    Optional<BulkItem> existing = bulkItemRepository
                        .findByUserAndHsCodeAndDescriptionAndIsActive(
                            user, hsCode, description, true);
                    
                    if (existing.isPresent()) {
                        errors.add("라인 " + rowNumber + ": 이미 등록된 품목입니다. (" + description + ")");
                        continue;
                    }

                    BulkItem item = new BulkItem(user, hsCode, description, englishName);
                    
                    // 선택 필드 설정
                    if (columns.length > 3 && !columns[3].trim().isEmpty()) {
                        try {
                            item.setDefaultQuantity(Integer.parseInt(columns[3].trim()));
                        } catch (NumberFormatException e) {
                            errors.add("라인 " + rowNumber + ": 수량 형식이 올바르지 않습니다.");
                            continue;
                        }
                    }
                    
                    if (columns.length > 4 && !columns[4].trim().isEmpty()) {
                        try {
                            item.setDefaultWeight(new BigDecimal(columns[4].trim()));
                        } catch (NumberFormatException e) {
                            errors.add("라인 " + rowNumber + ": 중량 형식이 올바르지 않습니다.");
                            continue;
                        }
                    }
                    
                    if (columns.length > 5 && !columns[5].trim().isEmpty()) {
                        try {
                            item.setDefaultWidth(new BigDecimal(columns[5].trim()));
                        } catch (NumberFormatException e) {
                            errors.add("라인 " + rowNumber + ": 가로 크기 형식이 올바르지 않습니다.");
                            continue;
                        }
                    }
                    
                    if (columns.length > 6 && !columns[6].trim().isEmpty()) {
                        try {
                            item.setDefaultHeight(new BigDecimal(columns[6].trim()));
                        } catch (NumberFormatException e) {
                            errors.add("라인 " + rowNumber + ": 세로 크기 형식이 올바르지 않습니다.");
                            continue;
                        }
                    }
                    
                    if (columns.length > 7 && !columns[7].trim().isEmpty()) {
                        try {
                            item.setDefaultDepth(new BigDecimal(columns[7].trim()));
                        } catch (NumberFormatException e) {
                            errors.add("라인 " + rowNumber + ": 깊이 형식이 올바르지 않습니다.");
                            continue;
                        }
                    }
                    
                    if (columns.length > 8 && !columns[8].trim().isEmpty()) {
                        try {
                            item.setDefaultUnitPrice(new BigDecimal(columns[8].trim()));
                        } catch (NumberFormatException e) {
                            errors.add("라인 " + rowNumber + ": 단가 형식이 올바르지 않습니다.");
                            continue;
                        }
                    }
                    
                    if (columns.length > 9 && !columns[9].trim().isEmpty()) {
                        item.setCategory(columns[9].trim());
                    }

                    items.add(item);
                    successCount++;

                } catch (Exception e) {
                    log.error("라인 {} 처리 중 오류 발생", rowNumber, e);
                    errors.add("라인 " + rowNumber + ": " + e.getMessage());
                }
            }
        }

        // 성공한 레코드들만 저장
        if (!items.isEmpty()) {
            bulkItemRepository.saveAll(items);
            log.info("사용자 {}가 {} 개의 품목 정보를 업로드했습니다.", user.getEmail(), items.size());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalRows", totalRows - 1); // 헤더 제외
        result.put("successCount", successCount);
        result.put("errorCount", errors.size());
        result.put("errors", errors);

        return result;
    }

    /**
     * 품목 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getItemList(User user, int page, int size, String category) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BulkItem> itemPage;
        
        if (category != null && !category.trim().isEmpty()) {
            List<BulkItem> categoryItems = bulkItemRepository
                .findByUserAndCategoryAndIsActiveTrueOrderByDescriptionAsc(user, category);
            
            // 수동 페이징 처리 (간단한 구현)
            int start = page * size;
            int end = Math.min(start + size, categoryItems.size());
            List<BulkItem> pagedItems = start < categoryItems.size() ? 
                categoryItems.subList(start, end) : new ArrayList<>();
            
            Map<String, Object> result = new HashMap<>();
            result.put("items", pagedItems);
            result.put("totalElements", categoryItems.size());
            result.put("totalPages", (int) Math.ceil((double) categoryItems.size() / size));
            result.put("currentPage", page);
            result.put("size", size);
            
            return result;
        } else {
            itemPage = bulkItemRepository
                .findByUserAndIsActiveOrderByCreatedAtDesc(user, true, pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", itemPage.getContent());
        result.put("totalElements", itemPage.getTotalElements());
        result.put("totalPages", itemPage.getTotalPages());
        result.put("currentPage", page);
        result.put("size", size);

        // 추가 통계 정보
        Long totalCount = bulkItemRepository.countByUserAndIsActive(user, true);
        result.put("totalCount", totalCount);

        return result;
    }

    /**
     * 품목 검색
     */
    @Transactional(readOnly = true)
    public Map<String, Object> searchItems(User user, String keyword) {
        List<BulkItem> items = bulkItemRepository.searchByKeyword(user, keyword);
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("totalCount", items.size());
        
        return result;
    }

    /**
     * 품목 정보 수정
     */
    public Map<String, Object> updateItem(User user, Long itemId, Map<String, Object> updateData) {
        BulkItem item = bulkItemRepository.findByIdAndUser(itemId, user)
            .orElseThrow(() -> new RuntimeException("품목 정보를 찾을 수 없습니다."));

        // 업데이트 가능한 필드들
        if (updateData.containsKey("hsCode")) {
            item.setHsCode((String) updateData.get("hsCode"));
        }
        if (updateData.containsKey("description")) {
            item.setDescription((String) updateData.get("description"));
        }
        if (updateData.containsKey("englishName")) {
            item.setEnglishName((String) updateData.get("englishName"));
        }
        if (updateData.containsKey("defaultQuantity")) {
            item.setDefaultQuantity((Integer) updateData.get("defaultQuantity"));
        }
        if (updateData.containsKey("defaultWeight")) {
            item.setDefaultWeight(new BigDecimal(updateData.get("defaultWeight").toString()));
        }
        if (updateData.containsKey("defaultWidth")) {
            item.setDefaultWidth(new BigDecimal(updateData.get("defaultWidth").toString()));
        }
        if (updateData.containsKey("defaultHeight")) {
            item.setDefaultHeight(new BigDecimal(updateData.get("defaultHeight").toString()));
        }
        if (updateData.containsKey("defaultDepth")) {
            item.setDefaultDepth(new BigDecimal(updateData.get("defaultDepth").toString()));
        }
        if (updateData.containsKey("defaultUnitPrice")) {
            item.setDefaultUnitPrice(new BigDecimal(updateData.get("defaultUnitPrice").toString()));
        }
        if (updateData.containsKey("category")) {
            item.setCategory((String) updateData.get("category"));
        }
        if (updateData.containsKey("notes")) {
            item.setNotes((String) updateData.get("notes"));
        }

        BulkItem savedItem = bulkItemRepository.save(item);
        
        Map<String, Object> result = new HashMap<>();
        result.put("item", savedItem);
        
        return result;
    }

    /**
     * 품목 정보 삭제 (논리적 삭제)
     */
    public void deleteItem(User user, Long itemId) {
        BulkItem item = bulkItemRepository.findByIdAndUser(itemId, user)
            .orElseThrow(() -> new RuntimeException("품목 정보를 찾을 수 없습니다."));

        item.deactivate();
        bulkItemRepository.save(item);
        
        log.info("사용자 {}가 품목 {} 정보를 삭제했습니다.", user.getEmail(), item.getDescription());
    }

    /**
     * 드롭다운용 활성 품목 목록
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getActiveItems(User user) {
        List<BulkItem> items = bulkItemRepository.findByUserAndIsActiveTrueOrderByDescriptionAsc(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("totalCount", items.size());
        
        return result;
    }

    /**
     * 품목 통계 정보
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getItemStats(User user) {
        Long totalCount = bulkItemRepository.countByUserAndIsActive(user, true);
        List<Object[]> categoryStats = bulkItemRepository.getItemCountByCategory(user);
        List<Object[]> hsCodeStats = bulkItemRepository.getItemCountByHsCode(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("categoryStats", categoryStats);
        result.put("hsCodeStats", hsCodeStats);
        
        return result;
    }

    /**
     * CSV 템플릿 생성
     */
    public byte[] generateCsvTemplate() {
        StringBuilder csv = new StringBuilder();
        
        // CSV 헤더 (한국어)
        csv.append("HS코드,품목명,영문명,수량,중량(kg),가로(cm),세로(cm),깊이(cm),단가(THB),카테고리\n");
        
        // 예시 데이터
        csv.append("8517120000,스마트폰,Smartphone,1,0.2,15,7.5,0.8,25000,전자제품\n");
        csv.append("6204620000,면바지,Cotton Pants,5,0.5,30,40,2,800,의류\n");
        csv.append("3304990000,화장품,Cosmetics,10,0.1,5,10,5,1200,화장품\n");

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * CSV 라인 파싱 (쉼표와 따옴표 처리)
     */
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(ch);
            }
        }
        
        result.add(currentField.toString());
        return result.toArray(new String[0]);
    }
}