package com.ysc.lms.multitenancy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(2) // After security scanning filter
@RequiredArgsConstructor
@Slf4j
public class TenantFilter implements Filter {

    private final TenantService tenantService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            String tenantId = extractTenantId(httpRequest);
            
            if (tenantId != null) {
                // Validate tenant
                if (!tenantService.isTenantActive(tenantId)) {
                    log.warn("Request from inactive tenant: {}", tenantId);
                    sendTenantError(httpResponse, "Tenant is not active", 403);
                    return;
                }
                
                TenantContext.setTenantId(tenantId);
                log.debug("Set tenant context: {}", tenantId);
                
                // Add tenant ID to response headers for debugging
                httpResponse.setHeader("X-Tenant-ID", tenantId);
            } else {
                // Set default tenant for requests without tenant specification
                TenantContext.setTenantId("default");
                log.debug("Using default tenant for request: {}", httpRequest.getRequestURI());
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error processing tenant context", e);
            sendTenantError(httpResponse, "Internal server error", 500);
        } finally {
            TenantContext.clear();
        }
    }

    private String extractTenantId(HttpServletRequest request) {
        // Strategy 1: Extract from header
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            return tenantId.trim().toLowerCase();
        }

        // Strategy 2: Extract from subdomain
        String host = request.getHeader("Host");
        if (host != null && host.contains(".")) {
            String subdomain = host.split("\\.")[0];
            if (!subdomain.equals("www") && !subdomain.equals("api")) {
                return subdomain.toLowerCase();
            }
        }

        // Strategy 3: Extract from URL path
        String path = request.getRequestURI();
        if (path.startsWith("/tenant/")) {
            String[] pathParts = path.split("/");
            if (pathParts.length > 2) {
                return pathParts[2].toLowerCase();
            }
        }

        // Strategy 4: Extract from query parameter
        String queryTenant = request.getParameter("tenant");
        if (queryTenant != null && !queryTenant.trim().isEmpty()) {
            return queryTenant.trim().toLowerCase();
        }

        // Strategy 5: Extract from JWT token (if available)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String tenantFromToken = extractTenantFromJWT(token);
            if (tenantFromToken != null) {
                return tenantFromToken.toLowerCase();
            }
        }

        return null; // Will use default tenant
    }

    private String extractTenantFromJWT(String token) {
        // Mock implementation - would decode JWT and extract tenant claim
        // In real implementation, this would parse the JWT token
        return null;
    }

    private void sendTenantError(HttpServletResponse response, String message, int statusCode) 
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"error\": \"Tenant Error\", \"message\": \"%s\", \"timestamp\": \"%s\"}", 
            message,
            java.time.LocalDateTime.now()
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Tenant Filter initialized");
    }

    @Override
    public void destroy() {
        log.info("Tenant Filter destroyed");
    }
}