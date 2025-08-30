package com.ysc.lms.config;

import com.ysc.lms.annotation.ApiVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

@Configuration
public class ApiVersioningConfig implements WebMvcConfigurer {

    @Bean
    public RequestMappingHandlerMapping apiVersionRequestMappingHandlerMapping() {
        return new ApiVersionRequestMappingHandlerMapping();
    }

    private static class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

        @Override
        protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
            RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
            
            if (info == null) {
                return null;
            }

            // 클래스 레벨 버전 확인
            ApiVersion classVersion = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
            // 메소드 레벨 버전 확인 (우선순위 높음)
            ApiVersion methodVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);

            String version = null;
            if (methodVersion != null) {
                version = methodVersion.value();
            } else if (classVersion != null) {
                version = classVersion.value();
            }

            if (version != null) {
                // 헤더 기반 버전 관리
                RequestMappingInfo versionInfo = RequestMappingInfo
                    .paths() // paths는 이미 설정되어 있음
                    .headers("Api-Version=" + version, "Accept-Version=" + version)
                    .build();
                
                info = versionInfo.combine(info);
            }

            return info;
        }
    }
}