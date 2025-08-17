package com.ycs.lms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && validateMockToken(jwt)) {
                // Mock JWT 토큰에서 사용자 정보 추출
                UserPrincipal userPrincipal = getUserFromMockToken(jwt);
                
                if (userPrincipal != null) {
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Set authentication for user: {}", userPrincipal.getEmail());
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean validateMockToken(String token) {
        // Mock 구현 - 실제로는 JWT 라이브러리 사용
        return StringUtils.hasText(token) && token.startsWith("mock_access_token");
    }
    
    private UserPrincipal getUserFromMockToken(String token) {
        // Mock 구현 - 실제로는 JWT에서 사용자 정보 추출
        if (token.contains("_1")) {
            return new UserPrincipal(1L, "admin@ycs.com", "관리자", "admin");
        } else if (token.contains("_2")) {
            return new UserPrincipal(2L, "warehouse@ycs.com", "창고관리자", "warehouse");
        } else if (token.contains("_3")) {
            return new UserPrincipal(3L, "user1@example.com", "김개인", "individual");
        } else if (token.contains("_4")) {
            return new UserPrincipal(4L, "company@corp.com", "이기업", "enterprise");
        } else if (token.contains("_5")) {
            return new UserPrincipal(5L, "partner@affiliate.com", "박파트너", "partner");
        }
        return null;
    }
}