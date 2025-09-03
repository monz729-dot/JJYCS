package com.ysc.lms.config;

import com.ysc.lms.service.UserService;
import com.ysc.lms.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Authorization 헤더가 없거나 Bearer로 시작하지 않는 경우
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = jwtUtil.extractTokenFromHeader(authHeader);
        
        try {
            // JWT 토큰 유효성 사전 검사
            if (!jwtUtil.isTokenValid(jwt)) {
                log.warn("Invalid JWT token detected: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":false,\"error\":\"유효하지 않은 토큰입니다.\",\"code\":\"INVALID_TOKEN\"}");
                return;
            }
            
            userEmail = jwtUtil.getUsernameFromToken(jwt);
            
            // 이미 인증된 사용자가 아닌 경우만 처리
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                var userOpt = userService.findByEmail(userEmail);
                
                if (userOpt.isPresent()) {
                    var user = userOpt.get();
                    
                    // Refresh 토큰인지 확인 (access token만 허용)
                    if (jwtUtil.isRefreshToken(jwt)) {
                        log.warn("Refresh token used for authentication: {}", userEmail);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // 토큰 유효성 검증
                    if (jwtUtil.validateToken(jwt, userEmail)) {
                        
                        // UserDetails로 변환 - PENDING 상태도 허용
                        String role;
                        if (user.getStatus() == com.ysc.lms.entity.User.UserStatus.PENDING) {
                            // PENDING 상태의 사용자는 PENDING 역할 부여
                            role = "ROLE_PENDING";
                        } else {
                            // 활성 상태의 사용자는 원래 역할 부여
                            role = "ROLE_" + user.getUserType().toString();
                        }
                        log.debug("Creating UserDetails with role: {} for user: {} (status: {})", role, userEmail, user.getStatus());
                        
                        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                                .username(user.getEmail())
                                .password(user.getPassword())
                                .authorities(role)
                                .accountExpired(false)
                                .accountLocked(user.getStatus() == com.ysc.lms.entity.User.UserStatus.SUSPENDED || 
                                              user.getStatus() == com.ysc.lms.entity.User.UserStatus.DELETED)
                                .credentialsExpired(false)
                                .disabled(user.getStatus() == com.ysc.lms.entity.User.UserStatus.WITHDRAWN ||
                                         user.getStatus() == com.ysc.lms.entity.User.UserStatus.DELETED)
                                .build();
                        
                        // 인증 토큰 생성
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // SecurityContext에 인증 정보 설정
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        log.debug("JWT authentication successful for user: {}", userEmail);
                    } else {
                        log.warn("JWT token validation failed for user: {}", userEmail);
                    }
                } else {
                    log.warn("User not found for JWT token: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
            // JWT 토큰이 있지만 유효하지 않은 경우 401 응답
            if (jwt != null && !jwt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":false,\"error\":\"인증에 실패했습니다. 다시 로그인해주세요.\",\"code\":\"AUTHENTICATION_FAILED\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath(); // 중요! getRequestURI() 대신 getServletPath() 사용
        
        // OPTIONS 요청은 항상 우회
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        // 인증이 필요없는 경로들
        return path.startsWith("/auth/") ||
               path.startsWith("/h2-console") ||      // 핵심: servletPath 기준
               path.startsWith("/api/h2-console") ||  // 혹시 모를 구현 차이 대비(옵션)
               path.startsWith("/public/") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/hscode/") ||         // HS Code API 경로 추가
               path.equals("/favicon.ico");
    }
}