package com.ysc.lms.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * 조건부 필수 필드 검증기
 */
public class ConditionalRequiredValidator implements ConstraintValidator<ConditionalRequired, Object> {
    
    private ConditionalRequired.Rule[] rules;
    
    @Override
    public void initialize(ConditionalRequired constraintAnnotation) {
        this.rules = constraintAnnotation.rules();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null 객체는 다른 validation에서 처리
        }
        
        boolean isValid = true;
        context.disableDefaultConstraintViolation();
        
        for (ConditionalRequired.Rule rule : rules) {
            try {
                // 조건 필드 값 가져오기
                Object conditionValue = getFieldValue(value, rule.conditionField());
                
                // 조건이 만족되는지 확인
                if (conditionValue != null && 
                    Arrays.asList(rule.conditionValues()).contains(conditionValue.toString())) {
                    
                    // 필수 필드들을 검증
                    for (String requiredField : rule.requiredFields()) {
                        Object fieldValue = getFieldValue(value, requiredField);
                        
                        if (isEmpty(fieldValue)) {
                            isValid = false;
                            
                            // 구체적인 에러 메시지 생성
                            String fieldName = getFieldDisplayName(requiredField);
                            String userType = getUserTypeDisplayName(conditionValue.toString());
                            String message = String.format("%s 회원은 %s을(를) 반드시 입력해야 합니다.", 
                                userType, fieldName);
                            
                            context.buildConstraintViolationWithTemplate(message)
                                   .addPropertyNode(requiredField)
                                   .addConstraintViolation();
                        }
                    }
                }
                
            } catch (Exception e) {
                // 리플렉션 오류 시 검증 실패로 처리
                context.buildConstraintViolationWithTemplate("검증 오류가 발생했습니다.")
                       .addConstraintViolation();
                return false;
            }
        }
        
        return isValid;
    }
    
    /**
     * 리플렉션을 사용해 필드 값을 가져옵니다
     */
    private Object getFieldValue(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
    
    /**
     * 필드 값이 비어있는지 확인합니다
     */
    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }
        return false;
    }
    
    /**
     * 필드명을 사용자 친화적인 이름으로 변환합니다
     */
    private String getFieldDisplayName(String fieldName) {
        switch (fieldName) {
            case "companyName": return "회사명";
            case "businessNumber": return "사업자등록번호";
            case "companyAddress": return "회사 주소";
            case "contactPerson": return "담당자명";
            case "contactPhone": return "담당자 전화번호";
            default: return fieldName;
        }
    }
    
    /**
     * 사용자 유형을 사용자 친화적인 이름으로 변환합니다
     */
    private String getUserTypeDisplayName(String userType) {
        switch (userType) {
            case "CORPORATE": return "기업";
            case "PARTNER": return "파트너";
            case "WAREHOUSE": return "창고";
            case "ADMIN": return "관리자";
            default: return "일반";
        }
    }
}