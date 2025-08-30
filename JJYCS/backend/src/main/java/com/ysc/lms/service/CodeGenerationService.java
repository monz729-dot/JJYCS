package com.ysc.lms.service;

import com.ysc.lms.entity.User;
import com.ysc.lms.repository.UserRepository;
import com.ysc.lms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 고유코드/주문코드 생성 서비스
 * 규칙: A(항공)/M(해상) + R(리팩옵션 유무) + 국가코드(K=한국,T=태국) + 회원유형(P=개인,C=기업) + 일련번호
 * 예: RAKYP001 = 리팩+항공 / 한국 개인 / 일련 001
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CodeGenerationService {
    
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    
    /**
     * 회원 고유코드 생성 (회원가입 시 즉시 발급)
     * 규칙: 국가코드 + 회원유형 + 일련번호 (예: KYP001, KYC001, THP001)
     */
    @Transactional
    public String generateMemberCode(String country, User.UserType userType) {
        String countryCode = getCountryCode(country);
        String typeCode = getUserTypeCode(userType);
        String prefix = countryCode + typeCode;
        
        int sequence = getNextMemberSequence(prefix);
        String memberCode = String.format("%s%03d", prefix, sequence);
        
        log.info("Generated member code: {} for country: {}, type: {}", memberCode, country, userType);
        return memberCode;
    }
    
    /**
     * 주문코드 생성 (주문 시 운송수단/옵션에 따라 자동 부여)
     * 규칙: A/M + R? + 회원코드 + 일련번호
     * 예: AKYP001001 (항공+한국개인+001번회원+001번주문), MRAKYP001002 (리팩+해상+한국개인+001번회원+002번주문)
     */
    @Transactional
    public String generateOrderCode(String transportType, boolean hasRepackOption, String memberCode) {
        StringBuilder prefix = new StringBuilder();
        
        // 운송수단 코드
        String transportCode = getTransportCode(transportType);
        
        // 리팩 옵션이 있으면 R 추가
        if (hasRepackOption) {
            prefix.append("R");
        }
        
        prefix.append(transportCode);
        prefix.append(memberCode);
        
        int sequence = getNextOrderSequence(prefix.toString());
        String orderCode = String.format("%s%03d", prefix, sequence);
        
        log.info("Generated order code: {} for transport: {}, repack: {}, member: {}", 
                orderCode, transportType, hasRepackOption, memberCode);
        return orderCode;
    }
    
    /**
     * HBL 번호 생성 (사내 규칙)
     * 규칙: HBL + 년도 + 월 + 일련번호 (예: HBL240830001)
     */
    @Transactional
    public String generateHblNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String prefix = "HBL" + dateStr;
        
        int sequence = getNextHblSequence(prefix);
        String hblNumber = String.format("%s%03d", prefix, sequence);
        
        log.info("Generated HBL number: {}", hblNumber);
        return hblNumber;
    }
    
    /**
     * 코드 파싱 정보 제공 (관리자용 뷰)
     */
    public CodeInfo parseCode(String code) {
        CodeInfo info = new CodeInfo();
        info.setOriginalCode(code);
        
        if (code.startsWith("HBL")) {
            // HBL 파싱
            info.setCodeType("HBL");
            if (code.length() >= 12) {
                String dateStr = code.substring(3, 9);
                String sequence = code.substring(9);
                info.setIssuedDate("20" + dateStr.substring(0, 2) + "-" + 
                                   dateStr.substring(2, 4) + "-" + 
                                   dateStr.substring(4, 6));
                info.setSequenceNumber(sequence);
            }
        } else if (code.length() >= 6) {
            // 회원코드 또는 주문코드 파싱
            boolean hasRepack = code.startsWith("R");
            int offset = hasRepack ? 1 : 0;
            
            if (code.length() >= 6 + offset) {
                char transportChar = code.charAt(offset);
                if (transportChar == 'A' || transportChar == 'M') {
                    // 주문코드 파싱
                    info.setCodeType("ORDER");
                    info.setHasRepack(hasRepack);
                    info.setTransportType(transportChar == 'A' ? "항공" : "해상");
                    
                    String remaining = code.substring(offset + 1);
                    if (remaining.length() >= 3) {
                        String countryCode = remaining.substring(0, 1);
                        String typeCode = remaining.substring(1, 2);
                        
                        info.setCountry(countryCode.equals("K") ? "한국" : "태국");
                        info.setUserType(typeCode.equals("P") ? "개인" : "기업");
                        
                        // 회원번호와 주문번호 분리 (뒤 3자리가 주문번호)
                        if (remaining.length() >= 6) {
                            String memberSeq = remaining.substring(2, remaining.length() - 3);
                            String orderSeq = remaining.substring(remaining.length() - 3);
                            info.setMemberSequence(memberSeq);
                            info.setSequenceNumber(orderSeq);
                        }
                    }
                } else {
                    // 회원코드 파싱
                    info.setCodeType("MEMBER");
                    String countryCode = code.substring(0, 1);
                    String typeCode = code.substring(1, 2);
                    String sequence = code.substring(2);
                    
                    info.setCountry(countryCode.equals("K") ? "한국" : "태국");
                    info.setUserType(typeCode.equals("P") ? "개인" : "기업");
                    info.setSequenceNumber(sequence);
                }
            }
        }
        
        return info;
    }
    
    private String getCountryCode(String country) {
        if ("THAILAND".equalsIgnoreCase(country) || "TH".equalsIgnoreCase(country) || "태국".equals(country)) {
            return "T";
        }
        return "K"; // 기본값: 한국
    }
    
    private String getUserTypeCode(User.UserType userType) {
        return switch (userType) {
            case CORPORATE -> "C";
            case PARTNER -> "C"; // 파트너도 기업으로 처리
            default -> "P"; // GENERAL 등은 개인으로 처리
        };
    }
    
    private String getTransportCode(String transportType) {
        if ("air".equalsIgnoreCase(transportType) || "항공".equals(transportType)) {
            return "A";
        }
        return "M"; // 기본값: 해상
    }
    
    private int getNextMemberSequence(String prefix) {
        // 해당 prefix로 시작하는 가장 큰 시퀀스 조회
        String maxCode = userRepository.findMaxMemberCodeByPrefix(prefix);
        if (maxCode == null) {
            return 1;
        }
        
        String sequencePart = maxCode.substring(prefix.length());
        try {
            return Integer.parseInt(sequencePart) + 1;
        } catch (NumberFormatException e) {
            log.warn("Invalid sequence in member code: {}", maxCode);
            return 1;
        }
    }
    
    private int getNextOrderSequence(String prefix) {
        String maxCode = orderRepository.findMaxOrderCodeByPrefix(prefix + "%");
        if (maxCode == null) {
            return 1;
        }
        
        // 뒤 3자리가 주문 시퀀스
        String sequencePart = maxCode.substring(maxCode.length() - 3);
        try {
            return Integer.parseInt(sequencePart) + 1;
        } catch (NumberFormatException e) {
            log.warn("Invalid sequence in order code: {}", maxCode);
            return 1;
        }
    }
    
    private int getNextHblSequence(String prefix) {
        // TODO: HBL 테이블이 생성되면 실제 구현
        // 임시로 1 반환
        return 1;
    }
    
    /**
     * 코드 정보 DTO
     */
    public static class CodeInfo {
        private String originalCode;
        private String codeType; // MEMBER, ORDER, HBL
        private String country;
        private String userType;
        private String transportType;
        private Boolean hasRepack;
        private String memberSequence;
        private String sequenceNumber;
        private String issuedDate;
        
        // Getters and Setters
        public String getOriginalCode() { return originalCode; }
        public void setOriginalCode(String originalCode) { this.originalCode = originalCode; }
        public String getCodeType() { return codeType; }
        public void setCodeType(String codeType) { this.codeType = codeType; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
        public String getTransportType() { return transportType; }
        public void setTransportType(String transportType) { this.transportType = transportType; }
        public Boolean getHasRepack() { return hasRepack; }
        public void setHasRepack(Boolean hasRepack) { this.hasRepack = hasRepack; }
        public String getMemberSequence() { return memberSequence; }
        public void setMemberSequence(String memberSequence) { this.memberSequence = memberSequence; }
        public String getSequenceNumber() { return sequenceNumber; }
        public void setSequenceNumber(String sequenceNumber) { this.sequenceNumber = sequenceNumber; }
        public String getIssuedDate() { return issuedDate; }
        public void setIssuedDate(String issuedDate) { this.issuedDate = issuedDate; }
    }
}