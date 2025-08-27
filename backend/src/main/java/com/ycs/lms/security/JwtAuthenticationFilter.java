package com.ycs.lms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final AuthenticationEntryPoint unauthorizedHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = resolveToken(request);

            // 토큰이 있으면 검증 후 SecurityContext 세팅
            if (StringUtils.hasText(jwt)) {
                if (!tokenProvider.validateToken(jwt)) {
                    throw new BadCredentialsException("Invalid or expired JWT");
                }
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                if (authentication == null) {
                    throw new BadCredentialsException("Failed to create authentication from JWT");
                }
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (AuthenticationException ae) {
            // 인증 관련 예외 → 401
            SecurityContextHolder.clearContext();
            unauthorizedHandler.commence(request, response, ae);

        } catch (Exception e) {
            // 그 외 예외도 전부 401로 변환
            log.error("Unexpected JWT processing error", e);
            SecurityContextHolder.clearContext();
            unauthorizedHandler.commence(request, response,
                    new BadCredentialsException("JWT processing failed"));
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }
}
