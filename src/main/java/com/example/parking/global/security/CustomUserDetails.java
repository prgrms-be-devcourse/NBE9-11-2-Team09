package com.example.parking.global.security;

import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String userEmail;
    private final String role;

    // UserDetails 인터페이스 구현
    public CustomUserDetails(Long userId, String userEmail, String role) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.role = role;
    }

    // [CUS-08] 로그인 - 계정이 활성화되어 있는지 여부
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
