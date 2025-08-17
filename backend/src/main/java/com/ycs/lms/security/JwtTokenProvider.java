// backend/src/main/java/com/ycs/lms/security/JwtTokenProvider.java
package com.ycs.lms.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private String secret;

    @Value("${app.jwt.expiration:86400000}")
    private long expirationMs;

    @Value("${app.jwt.issuer:ycs-lms}")
    private String issuer;

    @Value("${app.jwt.clock-skew:30}")
    private long clockSkew;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /* ===== 호환 API ===== */

    public String generateToken(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal up) {
            return generateAccessToken(up);
        }
        UserPrincipal fallback = UserPrincipal.builder()
                .id(null)
                .email(authentication.getName())
                .name(authentication.getName())
                .role(authentication.getAuthorities().stream().findFirst()
                        .map(GrantedAuthority::getAuthority).orElse("USER"))
                .memberCode(null)
                .password("")
                .authorities(authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toList()))
                .build();
        return generateAccessToken(fallback);
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + (expirationMs * 7));
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(issuer)
                .setAudience("ycs-lms-refresh")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .requireIssuer(issuer)
                    .setAllowedClockSkewSeconds(clockSkew)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid refresh JWT: {}", ex.getMessage());
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(clockSkew)
                .build()
                .parseClaimsJws(token)
                .getBody();
        try { return Long.valueOf(claims.getSubject()); } catch (NumberFormatException e) { return null; }
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("email", String.class);
    }

    /** ← 새로 추가: 필터에서 사용하는 시그니처 */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(clockSkew)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long id = null;
        try { id = Long.valueOf(claims.getSubject()); } catch (NumberFormatException ignored) {}

        String email = claims.get("email", String.class);
        String name = claims.get("name", String.class);
        String role = claims.get("role", String.class);
        String memberCode = claims.get("memberCode", String.class);

        @SuppressWarnings("unchecked")
        var authorities = (java.util.List<String>) claims.getOrDefault("authorities",
                role != null ? java.util.List.of(role) : java.util.List.of("USER"));

        UserPrincipal principal = UserPrincipal.builder()
                .id(id)
                .email(email != null ? email : String.valueOf(id))
                .name(name != null ? name : (email != null ? email : "user"))
                .role(role != null ? role : "USER")
                .memberCode(memberCode)
                .password("")
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    /* ===== 표준 API ===== */

    public String generateAccessToken(UserPrincipal userPrincipal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userPrincipal.getId());
        claims.put("email", userPrincipal.getEmail());
        claims.put("name", userPrincipal.getName());
        claims.put("role", userPrincipal.getRole());
        claims.put("memberCode", userPrincipal.getMemberCode());
        claims.put("authorities", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userPrincipal.getId() != null ? String.valueOf(userPrincipal.getId()) : userPrincipal.getEmail())
                .setIssuer(issuer)
                .setAudience("ycs-lms-access")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(UserPrincipal userPrincipal) {
        Long id = userPrincipal.getId();
        return generateRefreshToken(id != null ? id : -1L);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .requireIssuer(issuer)
                    .setAllowedClockSkewSeconds(clockSkew)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }
}
