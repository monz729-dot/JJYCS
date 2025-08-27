package com.ycs.lms.config;

import com.ycs.lms.service.UserService;
import com.ycs.lms.util.JwtUtil;
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
                        
                        // UserDetails로 변환
                        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                                .username(user.getEmail())
                                .password(user.getPassword())
                                .authorities("ROLE_" + user.getUserType().toString())
                                .accountExpired(false)
                                .accountLocked(user.getStatus() != com.ycs.lms.entity.User.UserStatus.ACTIVE)
                                .credentialsExpired(false)
                                .disabled(user.getStatus() != com.ycs.lms.entity.User.UserStatus.ACTIVE)
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
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // 인증이 필요없는 경로들
        return path.startsWith("/auth/") ||
               path.startsWith("/api/public/") ||
               path.startsWith("/h2-console") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.equals("/favicon.ico");
    }
}