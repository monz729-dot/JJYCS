# YCS LMS 보안 개선 가이드

## 🚨 즉시 수정 필요한 보안 이슈

### 1. CORS 설정 개선
**현재 상태**: 모든 Origin 허용 (매우 위험)
**위치**: `backend/src/main/java/com/ycs/lms/config/CorsConfig.java`

```java
// 현재 (위험)
configuration.setAllowedOriginPatterns(Arrays.asList("*"));
configuration.setAllowCredentials(true);

// 수정 필요
configuration.setAllowedOrigins(Arrays.asList(
    "https://yourdomain.com",
    "https://admin.yourdomain.com"
));
// 개발환경에서만 localhost 허용
```

### 2. JWT 시크릿 키 보안
**현재 상태**: 하드코딩된 시크릿 키
**위치**: `backend/src/main/resources/application.yml`

```yaml
# 현재 (위험)
jwt:
  secret: YCS-LMS-2024-SECRET-KEY-FOR-JWT-TOKEN-GENERATION-MUST-BE-LONG-ENOUGH

# 수정 필요
jwt:
  secret: ${JWT_SECRET_KEY:}
```

**환경변수 설정**:
```bash
export JWT_SECRET_KEY="your-randomly-generated-64-char-secret-key-here"
```

### 3. SMTP 인증정보 보안
**현재 상태**: 평문으로 노출
**위치**: `backend/src/main/resources/application.yml`

```yaml
# 현재 (위험)
mail:
  username: monz729@gmail.com
  password: eyubylejwyuqnqvj

# 수정 필요
mail:
  username: ${SMTP_USERNAME:}
  password: ${SMTP_PASSWORD:}
```

### 4. H2 Console 운영환경 제거
**현재 상태**: 모든 환경에서 접근 가능
**위치**: `backend/src/main/java/com/ycs/lms/config/SecurityConfig.java`

```java
// 수정 필요: 개발환경에서만 활성화
@Bean
@Order(0)
@Profile("dev")  // 이 라인 추가
public SecurityFilterChain h2ConsoleChain(HttpSecurity http) throws Exception {
    // 기존 코드 유지
}
```

## 🛡️ 추가 보안 강화 방안

### 1. 패스워드 정책 구현

**파일 생성**: `backend/src/main/java/com/ycs/lms/security/PasswordPolicy.java`

```java
@Component
public class PasswordPolicy {
    private static final int MIN_LENGTH = 10;
    private static final Pattern COMPLEXITY_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$"
    );
    
    public void validate(String password) {
        if (password.length() < MIN_LENGTH) {
            throw new ValidationException("비밀번호는 최소 10자 이상이어야 합니다.");
        }
        if (!COMPLEXITY_PATTERN.matcher(password).matches()) {
            throw new ValidationException("비밀번호는 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.");
        }
    }
}
```

### 2. JWT 토큰 페이로드 최소화

**위치**: `backend/src/main/java/com/ycs/lms/util/JwtUtil.java`

```java
// 현재 (정보 과다 노출)
claims.put("email", user.getEmail());
claims.put("name", user.getName());
claims.put("memberCode", user.getMemberCode());

// 개선안 (최소한의 정보만)
claims.put("sub", user.getId().toString());
claims.put("role", user.getUserType().toString());
// 추가 정보는 별도 API로 조회
```

### 3. Refresh Token 저장소 구현

**파일 생성**: `backend/src/main/java/com/ycs/lms/security/RefreshTokenService.java`

```java
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void storeRefreshToken(String userId, String refreshToken) {
        redisTemplate.opsForValue().set(
            "refresh_token:" + userId, 
            refreshToken, 
            Duration.ofDays(7)
        );
    }
    
    public boolean isValidRefreshToken(String userId, String token) {
        String stored = redisTemplate.opsForValue().get("refresh_token:" + userId);
        return token.equals(stored);
    }
    
    public void revokeRefreshToken(String userId) {
        redisTemplate.delete("refresh_token:" + userId);
    }
}
```

### 4. 보안 헤더 설정

**파일 생성**: `backend/src/main/java/com/ycs/lms/config/SecurityHeaderFilter.java`

```java
@Component
public class SecurityHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 보안 헤더 설정
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // HTTPS 환경에서만 적용
        if (request.isSecure()) {
            httpResponse.setHeader("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains; preload");
        }
        
        chain.doFilter(request, response);
    }
}
```

### 5. Rate Limiting 구현

**파일 생성**: `backend/src/main/java/com/ycs/lms/config/RateLimitingFilter.java`

```java
@Component
public class RateLimitingFilter implements Filter {
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @PostConstruct
    public void init() {
        // 매분마다 카운터 초기화
        scheduler.scheduleAtFixedRate(requestCounts::clear, 1, 1, TimeUnit.MINUTES);
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        String clientIP = getClientIP((HttpServletRequest) request);
        
        AtomicInteger count = requestCounts.computeIfAbsent(clientIP, k -> new AtomicInteger(0));
        
        if (count.incrementAndGet() > 100) { // 분당 100회 제한
            ((HttpServletResponse) response).setStatus(429);
            ((HttpServletResponse) response).getWriter().write(
                "{\"error\":\"Too many requests. Please try again later.\"}"
            );
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

## 📋 보안 체크리스트

### 즉시 수정 (Priority 1)
- [ ] CORS 설정 수정 - 특정 도메인만 허용
- [ ] JWT 시크릿 키 환경변수 이전
- [ ] SMTP 인증정보 환경변수 이전
- [ ] H2 Console 개발환경 전용으로 제한

### 단기 개선 (Priority 2)
- [ ] 패스워드 정책 구현
- [ ] JWT 토큰 페이로드 최소화
- [ ] 보안 헤더 설정
- [ ] Rate Limiting 구현

### 중장기 개선 (Priority 3)
- [ ] Refresh Token 저장소 구현
- [ ] 계정 잠금 정책
- [ ] 보안 감사 로그 강화
- [ ] 의존성 취약점 스캔 자동화

## 🔒 환경별 보안 설정

### 개발환경 (application-dev.yml)
```yaml
spring:
  h2:
    console:
      enabled: true
      
cors:
  allowed-origins:
    - http://localhost:3008
    - http://127.0.0.1:3008

logging:
  level:
    com.ycs.lms: DEBUG
    org.springframework.security: DEBUG
```

### 운영환경 (application-prod.yml)
```yaml
spring:
  h2:
    console:
      enabled: false
      
cors:
  allowed-origins:
    - https://yourdomain.com
    - https://admin.yourdomain.com

logging:
  level:
    com.ycs.lms: INFO
    org.springframework.security: WARN
    root: WARN
```

## ⚠️ 보안 모니터링

### 로그 모니터링 패턴
```bash
# 실패한 로그인 시도
grep "Bad credentials" logs/application.log

# JWT 토큰 오류
grep "JWT authentication error" logs/application.log

# Rate Limiting 발동
grep "Too many requests" logs/application.log

# 권한 없는 접근 시도
grep "Access Denied" logs/application.log
```

### 보안 알림 설정
```yaml
# logback-spring.xml 예시
<appender name="SECURITY_APPENDER" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/security.log</file>
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>WARN</level>
    </filter>
</appender>

<logger name="SECURITY" level="WARN" additivity="false">
    <appender-ref ref="SECURITY_APPENDER"/>
</logger>
```

## 📞 보안 사고 대응

### 1단계: 즉시 대응
1. 의심스러운 활동 감지 시 해당 IP 차단
2. 관련 사용자 계정 임시 비활성화
3. 보안 로그 백업 및 분석

### 2단계: 근본 원인 분석
1. 로그 분석을 통한 공격 벡터 확인
2. 영향 범위 조사
3. 데이터 유출 여부 확인

### 3단계: 복구 및 강화
1. 취약점 패치 적용
2. 보안 정책 업데이트
3. 모니터링 강화

---

**⚠️ 중요**: 이 문서의 보안 개선사항들은 운영환경 배포 전에 반드시 적용되어야 합니다.

**마지막 업데이트**: 2024-08-29
**보안 정책 버전**: 1.0