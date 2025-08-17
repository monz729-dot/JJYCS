package com.ycs.lms.security;

import com.ycs.lms.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@ToString
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String email;
    private final String name;
    private final String role;        // e.g., "USER" or "ROLE_USER"
    private final String memberCode;  // nullable
    private final String password;
    private final List<String> authorities; // e.g., ["USER"]

    /** 기존 서비스 코드가 기대하는 정적 팩토리 */
    public static UserPrincipal create(User user) {
        String role = user.getRole() != null ? user.getRole().name() : "USER";
        String memberCode = null;
        try { memberCode = user.getMemberCode(); } catch (Exception ignored) {}
        String passwordHash = null;
        try { passwordHash = user.getPasswordHash(); } catch (Exception ignored) {}

        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(role)
                .memberCode(memberCode)
                .password(passwordHash != null ? passwordHash : "")
                .authorities(List.of(role))
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) return List.of(new SimpleGrantedAuthority(role));
        return authorities.stream()
                .filter(Objects::nonNull)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
