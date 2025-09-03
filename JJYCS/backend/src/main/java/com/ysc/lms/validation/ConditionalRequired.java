package com.ysc.lms.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 조건부 필수 필드 검증 어노테이션
 * userType에 따라 특정 필드가 필수인지 검증합니다.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalRequiredValidator.class)
@Documented
public @interface ConditionalRequired {
    
    String message() default "필수 필드를 입력해주세요";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 조건부 검증 규칙들
     */
    Rule[] rules() default {};
    
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Rule {
        /**
         * 조건 필드 (예: userType)
         */
        String conditionField();
        
        /**
         * 조건 값 (예: CORPORATE, PARTNER)
         */
        String[] conditionValues();
        
        /**
         * 필수 필드들 (조건이 만족될 때 필수가 되는 필드들)
         */
        String[] requiredFields();
        
        /**
         * 에러 메시지
         */
        String message() default "필수 필드를 입력해주세요";
    }
    
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        ConditionalRequired[] value();
    }
}