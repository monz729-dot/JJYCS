package com.ycs.lms.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.clock-skew}")
    private long clockSkew;

    private Key getSigningKey() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret key is not configured. Please set JWT_SECRET environment variable.");
        }
        
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            
            // HMAC-SHA512를 위해 최소 512비트(64바이트) 키 길이 검증
            if (keyBytes.length < 64) {
                throw new IllegalStateException("JWT secret key must be at least 512 bits (64 bytes) for HMAC-SHA512");
            }
            
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT secret key must be a valid Base64 encoded string", e);
        }
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userPrincipal.getId());
        claims.put("email", userPrincipal.getEmail());
        claims.put("name", userPrincipal.getName());
        claims.put("role", userPrincipal.getRole());
        claims.put("memberCode", userPrincipal.getMemberCode());
        claims.put("authorities", userPrincipal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(Long.toString(userPrincipal.getId()))
            .setIssuer(issuer)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .setAudience("ycs-lms-client")
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);
        
        return Jwts.builder()
            .setSubject(Long.toString(userId))
            .setIssuer(issuer)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .setAudience("ycs-lms-refresh")
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        return Long.parseLong(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        return claims.get("email", String.class);
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .requireIssuer(issuer)
                .requireAudience("ycs-lms-client")
                .setAllowedClockSkewSeconds(clockSkew)
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
        }
        return false;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .requireIssuer(issuer)
            .setAllowedClockSkewSeconds(clockSkew)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    /**
     * 리프레시 토큰 검증
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .requireIssuer(issuer)
                .requireAudience("ycs-lms-refresh")
                .setAllowedClockSkewSeconds(clockSkew)
                .build()
                .parseClaimsJws(refreshToken);
            return true;
        } catch (Exception ex) {
            log.error("Refresh token validation failed: {}", ex.getMessage());
            return false;
        }
    }
}