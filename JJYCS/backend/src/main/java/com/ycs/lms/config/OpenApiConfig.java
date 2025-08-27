package com.ycs.lms.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "YCS LMS API",
        version = "v1.0.0",
        description = "YCS 물류관리시스템(Logistics Management System) REST API 문서",
        contact = @Contact(
            name = "YCS Development Team",
            email = "dev@ycs.co.kr",
            url = "https://www.ycs.co.kr"
        ),
        license = @License(
            name = "Proprietary",
            url = "https://www.ycs.co.kr/license"
        )
    ),
    servers = {
        @Server(url = "/", description = "Default Server URL"),
        @Server(url = "https://api.ycs-lms.com", description = "Production Server"),
        @Server(url = "https://staging-api.ycs-lms.com", description = "Staging Server")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class OpenApiConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new io.swagger.v3.oas.models.info.Info()
                .title("YCS LMS API")
                .version("v1.0.0")
                .description("""
                    # YCS 물류관리시스템 API
                    
                    ## 개요
                    YCS LMS는 물류 및 배송 관리를 위한 종합적인 시스템입니다.
                    
                    ## 인증
                    대부분의 API는 JWT 토큰을 통한 인증이 필요합니다.
                    
                    ### 인증 방법
                    1. `/api/v1/auth/login` 엔드포인트로 로그인
                    2. 응답으로 받은 JWT 토큰을 Authorization 헤더에 포함
                    3. 형식: `Authorization: Bearer <token>`
                    
                    ## 사용자 역할
                    - **GENERAL**: 일반 개인 사용자
                    - **ENTERPRISE**: 기업 사용자
                    - **PARTNER**: 파트너 사용자
                    - **WAREHOUSE**: 창고 관리자
                    - **ADMIN**: 시스템 관리자
                    
                    ## 주요 기능
                    - 사용자 관리 (회원가입, 로그인, 프로필 관리)
                    - 주문 관리 (주문 생성, 상태 추적, 히스토리)
                    - 창고 관리 (입고, 출고, 재고 관리)
                    - 파일 관리 (업로드, 다운로드)
                    - 알림 시스템
                    - 외부 API 통합 (EMS 추적, 환율 조회, HS 코드 검증)
                    
                    ## 오류 처리
                    모든 오류 응답은 다음과 같은 구조를 가집니다:
                    ```json
                    {
                      "timestamp": "2023-12-01T12:00:00",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "상세 오류 메시지",
                      "errorCode": "VALIDATION_FAILED",
                      "path": "/api/v1/orders"
                    }
                    ```
                    
                    ## 페이징
                    목록 조회 API는 다음 파라미터를 지원합니다:
                    - `page`: 페이지 번호 (0부터 시작)
                    - `size`: 페이지 크기 (기본값: 20)
                    - `sort`: 정렬 기준 (예: createdAt,desc)
                    """))
            .servers(List.of(
                new io.swagger.v3.oas.models.servers.Server().url("/").description("Default Server URL"),
                new io.swagger.v3.oas.models.servers.Server().url("https://api.ycs-lms.com").description("Production Server"),
                new io.swagger.v3.oas.models.servers.Server().url("https://staging-api.ycs-lms.com").description("Staging Server")
            ))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("bearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme()
                    .name("bearerAuth")
                    .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT 토큰을 입력하세요")));
    }
}