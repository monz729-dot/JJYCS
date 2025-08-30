package com.ysc.lms.util;

import com.ysc.lms.exception.ValidationException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 입력 검증 유틸리티 클래스
 */
public class ValidationUtil {

    // 정규식 패턴들
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+82-?)?0?1[0-9]-?[0-9]{3,4}-?[0-9]{4}$");
    
    private static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile(
        "^YCS-\\d{6}-\\d{3}$");
    
    private static final Pattern HS_CODE_PATTERN = Pattern.compile(
        "^\\d{4}\\.\\d{2}$");
    
    private static final Pattern MEMBER_CODE_PATTERN = Pattern.compile(
        "^[A-Z]{3}\\d{3}$");
    
    private static final Pattern TRACKING_NUMBER_PATTERN = Pattern.compile(
        "^[A-Z]{2}\\d{9}[A-Z]{2}$");

    // SQL Injection 방지용 위험 문자들
    private static final String[] SQL_INJECTION_PATTERNS = {
        "'", "\"", ";", "--", "/*", "*/", "xp_", "sp_", "exec", "execute", 
        "select", "insert", "update", "delete", "drop", "create", "alter"
    };

    // XSS 방지용 위험 문자들  
    private static final String[] XSS_PATTERNS = {
        "<script", "</script>", "javascript:", "onload=", "onerror=", 
        "onclick=", "onmouseover=", "onfocus=", "onblur="
    };

    /**
     * 이메일 형식 검증
     */
    public static boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.toLowerCase()).matches();
    }

    /**
     * 전화번호 형식 검증
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.replaceAll("\\s", "")).matches();
    }

    /**
     * 주문번호 형식 검증
     */
    public static boolean isValidOrderNumber(String orderNumber) {
        if (!StringUtils.hasText(orderNumber)) {
            return false;
        }
        return ORDER_NUMBER_PATTERN.matcher(orderNumber).matches();
    }

    /**
     * HS 코드 형식 검증
     */
    public static boolean isValidHsCode(String hsCode) {
        if (!StringUtils.hasText(hsCode)) {
            return false;
        }
        return HS_CODE_PATTERN.matcher(hsCode).matches();
    }

    /**
     * 회원코드 형식 검증
     */
    public static boolean isValidMemberCode(String memberCode) {
        if (!StringUtils.hasText(memberCode)) {
            return false;
        }
        return MEMBER_CODE_PATTERN.matcher(memberCode).matches();
    }

    /**
     * EMS 추적번호 형식 검증
     */
    public static boolean isValidTrackingNumber(String trackingNumber) {
        if (!StringUtils.hasText(trackingNumber)) {
            return false;
        }
        return TRACKING_NUMBER_PATTERN.matcher(trackingNumber).matches();
    }

    /**
     * 파일명 안전성 검증
     */
    public static boolean isValidFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return false;
        }
        
        // 위험한 문자들 체크
        String[] dangerousChars = {"../", "..\\", "/", "\\", ":", "*", "?", "\"", "<", ">", "|"};
        for (String dangerousChar : dangerousChars) {
            if (fileName.contains(dangerousChar)) {
                return false;
            }
        }
        
        // 예약된 파일명 체크 (Windows)
        String[] reservedNames = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", 
                                  "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", 
                                  "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
        for (String reservedName : reservedNames) {
            if (fileName.toUpperCase().equals(reservedName)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * SQL Injection 패턴 검사
     */
    public static void checkSqlInjection(String input) {
        if (!StringUtils.hasText(input)) {
            return;
        }
        
        String lowerInput = input.toLowerCase();
        for (String pattern : SQL_INJECTION_PATTERNS) {
            if (lowerInput.contains(pattern.toLowerCase())) {
                throw new ValidationException(
                    "입력값에 허용되지 않는 문자가 포함되어 있습니다.");
            }
        }
    }

    /**
     * XSS 패턴 검사
     */
    public static void checkXss(String input) {
        if (!StringUtils.hasText(input)) {
            return;
        }
        
        String lowerInput = input.toLowerCase();
        for (String pattern : XSS_PATTERNS) {
            if (lowerInput.contains(pattern.toLowerCase())) {
                throw new ValidationException(
                    "입력값에 허용되지 않는 스크립트가 포함되어 있습니다.");
            }
        }
    }

    /**
     * 문자열 길이 검증
     */
    public static void validateLength(String value, String fieldName, int maxLength) {
        if (StringUtils.hasText(value) && value.length() > maxLength) {
            throw new ValidationException(
                String.format("%s는 %d자 이하로 입력해주세요.", fieldName, maxLength));
        }
    }

    /**
     * 필수값 검증
     */
    public static void validateRequired(Object value, String fieldName) {
        if (value == null || (value instanceof String && !StringUtils.hasText((String) value))) {
            throw new ValidationException(
                String.format("%s는 필수 입력 항목입니다.", fieldName));
        }
    }

    /**
     * 숫자 범위 검증
     */
    public static void validateRange(double value, String fieldName, double min, double max) {
        if (value < min || value > max) {
            throw new ValidationException(
                String.format("%s는 %.2f 이상 %.2f 이하로 입력해주세요.", fieldName, min, max));
        }
    }

    /**
     * 안전한 문자열로 정제 (HTML 이스케이프)
     */
    public static String sanitizeHtml(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
    }

    /**
     * 안전한 파일명으로 정제
     */
    public static String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return fileName;
        }
        
        // 위험한 문자들을 안전한 문자로 변경
        return fileName
            .replaceAll("[^a-zA-Z0-9\\.\\-_]", "_")
            .replaceAll("\\.{2,}", ".") // 연속된 점 제거
            .replaceAll("_{2,}", "_");  // 연속된 언더스코어 제거
    }

    /**
     * 이메일 유효성 검증 (예외 발생)
     */
    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new ValidationException.InvalidEmailFormatException(email);
        }
    }

    /**
     * 전화번호 유효성 검증 (예외 발생)
     */
    public static void validatePhoneNumber(String phone) {
        if (!isValidPhoneNumber(phone)) {
            throw new ValidationException.InvalidPhoneFormatException(phone);
        }
    }

    /**
     * HS 코드 유효성 검증 (예외 발생)
     */
    public static void validateHsCode(String hsCode) {
        if (!isValidHsCode(hsCode)) {
            throw new ValidationException.InvalidHsCodeException(hsCode);
        }
    }
}