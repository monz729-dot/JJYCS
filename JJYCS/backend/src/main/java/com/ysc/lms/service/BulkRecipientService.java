package com.ysc.lms.service;

import com.ysc.lms.entity.BulkRecipient;
import com.ysc.lms.entity.User;
import com.ysc.lms.repository.BulkRecipientRepository;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BulkRecipientService {

    private final BulkRecipientRepository bulkRecipientRepository;

    /**
     * CSV 파일로 수취인 정보 일괄 업로드
     */
    public Map<String, Object> uploadRecipients(User user, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("CSV 파일만 업로드 가능합니다.");
        }

        List<BulkRecipient> recipients = new ArrayList<>();
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
                    if (columns.length < 5) {
                        errors.add("라인 " + rowNumber + ": 필수 컬럼이 부족합니다. (이름, 전화번호, 주소, 우편번호, 국가)");
                        continue;
                    }

                    // 필수 필드 검증
                    String recipientName = columns[0].trim();
                    String recipientPhone = columns[1].trim();
                    String recipientAddress = columns[2].trim();
                    String recipientPostalCode = columns[3].trim();
                    String country = columns[4].trim();

                    if (recipientName.isEmpty() || recipientAddress.isEmpty() || country.isEmpty()) {
                        errors.add("라인 " + rowNumber + ": 필수 필드가 비어있습니다.");
                        continue;
                    }

                    // 중복 체크
                    Optional<BulkRecipient> existing = bulkRecipientRepository
                        .findByUserAndRecipientNameAndRecipientAddressAndIsActive(
                            user, recipientName, recipientAddress, true);
                    
                    if (existing.isPresent()) {
                        errors.add("라인 " + rowNumber + ": 이미 등록된 수취인입니다. (" + recipientName + ")");
                        continue;
                    }

                    BulkRecipient recipient = new BulkRecipient(user, recipientName, recipientPhone, 
                                                              recipientAddress, recipientPostalCode, country);
                    
                    // 선택 필드 설정
                    if (columns.length > 5 && !columns[5].trim().isEmpty()) {
                        recipient.setRecipientEmail(columns[5].trim());
                    }
                    if (columns.length > 6 && !columns[6].trim().isEmpty()) {
                        recipient.setCompany(columns[6].trim());
                    }
                    if (columns.length > 7 && !columns[7].trim().isEmpty()) {
                        recipient.setSpecialInstructions(columns[7].trim());
                    }

                    recipients.add(recipient);
                    successCount++;

                } catch (Exception e) {
                    log.error("라인 {} 처리 중 오류 발생", rowNumber, e);
                    errors.add("라인 " + rowNumber + ": " + e.getMessage());
                }
            }
        }

        // 성공한 레코드들만 저장
        if (!recipients.isEmpty()) {
            bulkRecipientRepository.saveAll(recipients);
            log.info("사용자 {}가 {} 개의 수취인 정보를 업로드했습니다.", user.getEmail(), recipients.size());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalRows", totalRows - 1); // 헤더 제외
        result.put("successCount", successCount);
        result.put("errorCount", errors.size());
        result.put("errors", errors);

        return result;
    }

    /**
     * 수취인 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRecipientList(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BulkRecipient> recipientPage = bulkRecipientRepository
            .findByUserAndIsActiveOrderByCreatedAtDesc(user, true, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("recipients", recipientPage.getContent());
        result.put("totalElements", recipientPage.getTotalElements());
        result.put("totalPages", recipientPage.getTotalPages());
        result.put("currentPage", page);
        result.put("size", size);

        // 추가 통계 정보
        Long totalCount = bulkRecipientRepository.countByUserAndIsActive(user, true);
        List<Object[]> countryStats = bulkRecipientRepository.getRecipientCountByCountry(user);
        
        result.put("totalCount", totalCount);
        result.put("countryStats", countryStats);

        return result;
    }

    /**
     * 수취인 정보 수정
     */
    public Map<String, Object> updateRecipient(User user, Long recipientId, Map<String, Object> updateData) {
        BulkRecipient recipient = bulkRecipientRepository.findByIdAndUser(recipientId, user)
            .orElseThrow(() -> new RuntimeException("수취인 정보를 찾을 수 없습니다."));

        // 업데이트 가능한 필드들
        if (updateData.containsKey("recipientName")) {
            recipient.setRecipientName((String) updateData.get("recipientName"));
        }
        if (updateData.containsKey("recipientPhone")) {
            recipient.setRecipientPhone((String) updateData.get("recipientPhone"));
        }
        if (updateData.containsKey("recipientAddress")) {
            recipient.setRecipientAddress((String) updateData.get("recipientAddress"));
        }
        if (updateData.containsKey("recipientPostalCode")) {
            recipient.setRecipientPostalCode((String) updateData.get("recipientPostalCode"));
        }
        if (updateData.containsKey("country")) {
            recipient.setCountry((String) updateData.get("country"));
        }
        if (updateData.containsKey("recipientEmail")) {
            recipient.setRecipientEmail((String) updateData.get("recipientEmail"));
        }
        if (updateData.containsKey("company")) {
            recipient.setCompany((String) updateData.get("company"));
        }
        if (updateData.containsKey("specialInstructions")) {
            recipient.setSpecialInstructions((String) updateData.get("specialInstructions"));
        }
        if (updateData.containsKey("notes")) {
            recipient.setNotes((String) updateData.get("notes"));
        }

        BulkRecipient savedRecipient = bulkRecipientRepository.save(recipient);
        
        Map<String, Object> result = new HashMap<>();
        result.put("recipient", savedRecipient);
        
        return result;
    }

    /**
     * 수취인 정보 삭제 (논리적 삭제)
     */
    public void deleteRecipient(User user, Long recipientId) {
        BulkRecipient recipient = bulkRecipientRepository.findByIdAndUser(recipientId, user)
            .orElseThrow(() -> new RuntimeException("수취인 정보를 찾을 수 없습니다."));

        recipient.deactivate();
        bulkRecipientRepository.save(recipient);
        
        log.info("사용자 {}가 수취인 {} 정보를 삭제했습니다.", user.getEmail(), recipient.getRecipientName());
    }

    /**
     * CSV 템플릿 생성
     */
    public byte[] generateCsvTemplate() {
        StringBuilder csv = new StringBuilder();
        
        // CSV 헤더 (한국어)
        csv.append("수취인 이름,전화번호,주소,우편번호,국가,이메일(선택),회사명(선택),특별요청사항(선택)\n");
        
        // 예시 데이터
        csv.append("홍길동,010-1234-5678,서울시 강남구 테헤란로 123,06142,대한민국,hong@example.com,ABC회사,문앞배송 요청\n");
        csv.append("김영희,010-9876-5432,부산시 해운대구 센텀로 456,48058,대한민국,kim@example.com,XYZ상사,오후배송 희망\n");

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

    /**
     * 키워드로 수취인 검색
     */
    @Transactional(readOnly = true)
    public List<BulkRecipient> searchRecipients(User user, String keyword) {
        return bulkRecipientRepository.searchByKeyword(user, keyword);
    }

    /**
     * 활성 수취인 목록 (드롭다운용)
     */
    @Transactional(readOnly = true)
    public List<BulkRecipient> getActiveRecipients(User user) {
        return bulkRecipientRepository.findByUserAndIsActiveTrueOrderByRecipientNameAsc(user);
    }
}