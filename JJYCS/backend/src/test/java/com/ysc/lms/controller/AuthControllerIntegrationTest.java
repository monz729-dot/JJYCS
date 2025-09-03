package com.ysc.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysc.lms.YscLmsApplication;
import com.ysc.lms.constants.ErrorCodes;
import com.ysc.lms.entity.User;
import com.ysc.lms.repository.UserRepository;
import com.ysc.lms.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 통합 계약 테스트
 * 실제 Spring Context와 함께 전체 회원가입 플로우를 테스트합니다
 */
@SpringBootTest(classes = YscLmsApplication.class)
@ActiveProfiles("test") 
@AutoConfigureWebMvc
@Transactional
class AuthControllerIntegrationTest {

    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build();
        
        // 테스트 전 기존 데이터 정리
        userRepository.deleteAll();
    }
    
    @Test
    @DisplayName("일반 회원가입 성공")
    void testGeneralUserSignupSuccess() throws Exception {
        // Given
        Map<String, Object> signupRequest = new HashMap<>();
        signupRequest.put("email", "test@example.com");
        signupRequest.put("password", "password123!");
        signupRequest.put("name", "테스트사용자");
        signupRequest.put("phone", "010-1234-5678");
        signupRequest.put("userType", "GENERAL");
        
        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.isPending").value(false))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.name").value("테스트사용자"))
                .andExpect(jsonPath("$.user.userType").value("GENERAL"))
                .andExpect(jsonPath("$.user.status").value("ACTIVE"))
                .andExpect(jsonPath("$.user.memberCode").exists())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
                
        // DB 검증
        Optional<User> savedUser = userService.findByEmail("test@example.com");
        assert savedUser.isPresent();
        assert savedUser.get().getUserType() == User.UserType.GENERAL;
        assert savedUser.get().getStatus() == User.UserStatus.ACTIVE;
    }
    
    @Test
    @DisplayName("기업 회원가입 성공 - 승인 대기")
    void testCorporateUserSignupSuccess() throws Exception {
        // Given
        Map<String, Object> signupRequest = new HashMap<>();
        signupRequest.put("email", "corporate@example.com");
        signupRequest.put("password", "password123!");
        signupRequest.put("name", "기업담당자");
        signupRequest.put("phone", "010-1234-5678");
        signupRequest.put("userType", "CORPORATE");
        signupRequest.put("companyName", "테스트기업");
        signupRequest.put("businessNumber", "123-45-67890");
        signupRequest.put("companyAddress", "서울시 강남구");
        
        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다. 기업/파트너 계정은 관리자 승인 후 이용 가능합니다. (평일 1~2일 소요)"))
                .andExpect(jsonPath("$.isPending").value(true))
                .andExpect(jsonPath("$.user.email").value("corporate@example.com"))
                .andExpect(jsonPath("$.user.userType").value("CORPORATE"))
                .andExpect(jsonPath("$.user.status").value("PENDING"))
                .andExpect(jsonPath("$.user.companyName").value("테스트기업"))
                .andExpect(jsonPath("$.user.businessNumber").value("123-45-67890"))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
                
        // DB 검증
        Optional<User> savedUser = userService.findByEmail("corporate@example.com");
        assert savedUser.isPresent();
        assert savedUser.get().getUserType() == User.UserType.CORPORATE;
        assert savedUser.get().getStatus() == User.UserStatus.PENDING;
        assert "테스트기업".equals(savedUser.get().getCompanyName());
    }
    
    @Test
    @DisplayName("기업 회원가입 실패 - 회사명 누락 (조건부 검증)")
    void testCorporateUserSignupFailureMissingCompanyName() throws Exception {
        // Given
        Map<String, Object> signupRequest = new HashMap<>();
        signupRequest.put("email", "corporate-fail@example.com");
        signupRequest.put("password", "password123!");
        signupRequest.put("name", "기업담당자");
        signupRequest.put("phone", "010-1234-5678");
        signupRequest.put("userType", "CORPORATE");
        // companyName과 businessNumber 누락
        
        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.error").value("기업 회원은 회사명을 반드시 입력해야 합니다."));
    }
    
    @Test
    @DisplayName("파트너 회원가입 실패 - 회사명 누락 (조건부 검증)")
    void testPartnerUserSignupFailureMissingCompanyName() throws Exception {
        // Given
        Map<String, Object> signupRequest = new HashMap<>();
        signupRequest.put("email", "partner-fail@example.com");
        signupRequest.put("password", "password123!");
        signupRequest.put("name", "파트너담당자");
        signupRequest.put("phone", "010-1234-5678");
        signupRequest.put("userType", "PARTNER");
        // companyName 누락
        
        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.error").value("파트너 회원은 회사명을 반드시 입력해야 합니다."));
    }
    
    @Test
    @DisplayName("중복 이메일 회원가입 실패")
    void testDuplicateEmailSignupFailure() throws Exception {
        // Given - 기존 사용자 생성
        Map<String, Object> firstRequest = new HashMap<>();
        firstRequest.put("email", "duplicate@example.com");
        firstRequest.put("password", "password123!");
        firstRequest.put("name", "첫번째사용자");
        firstRequest.put("phone", "010-1111-1111");
        firstRequest.put("userType", "GENERAL");
        
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());
        
        // When - 동일 이메일로 재가입 시도
        Map<String, Object> secondRequest = new HashMap<>();
        secondRequest.put("email", "duplicate@example.com");
        secondRequest.put("password", "password456!");
        secondRequest.put("name", "두번째사용자");
        secondRequest.put("phone", "010-2222-2222");
        secondRequest.put("userType", "GENERAL");
        
        // Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCodes.DUPLICATE_EMAIL))
                .andExpect(jsonPath("$.error").value("이미 가입된 이메일입니다."));
    }
    
    @Test
    @DisplayName("잘못된 이메일 형식 검증")
    void testInvalidEmailFormatValidation() throws Exception {
        // Given
        Map<String, Object> signupRequest = new HashMap<>();
        signupRequest.put("email", "invalid-email");
        signupRequest.put("password", "password123!");
        signupRequest.put("name", "테스트사용자");
        signupRequest.put("phone", "010-1234-5678");
        signupRequest.put("userType", "GENERAL");
        
        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.error").value("올바른 이메일 형식을 입력해주세요"));
    }
    
    @Test
    @DisplayName("비밀번호 길이 검증")
    void testPasswordLengthValidation() throws Exception {
        // Given
        Map<String, Object> signupRequest = new HashMap<>();
        signupRequest.put("email", "test@example.com");
        signupRequest.put("password", "short"); // 8자 미만
        signupRequest.put("name", "테스트사용자");
        signupRequest.put("phone", "010-1234-5678");
        signupRequest.put("userType", "GENERAL");
        
        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.error").value("비밀번호는 최소 8자 이상이어야 합니다"));
    }
    
    @Test
    @DisplayName("UTF-8 한글 회원가입 성공")
    void testUtf8KoreanSignupSuccess() throws Exception {
        // Given
        Map<String, Object> signupRequest = new HashMap<>();
        signupRequest.put("email", "한글이메일테스트@도메인.com");
        signupRequest.put("password", "한글비밀번호123!");
        signupRequest.put("name", "한글이름테스트");
        signupRequest.put("phone", "010-1234-5678");
        signupRequest.put("userType", "GENERAL");
        
        // When & Then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.email").value("한글이메일테스트@도메인.com"))
                .andExpect(jsonPath("$.user.name").value("한글이름테스트"));
    }
}