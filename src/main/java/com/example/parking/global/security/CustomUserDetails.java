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

    // [CUS-08] 로그인 - 로그인 시 이메일로 사용자 조회 후 CustomUserDetails 객체 생성
    private final User user;

    // UserDetails 인터페이스 구현
    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getUserEmail() {
        return user.getEmail();
    }

    // [CUS-08] 로그인 - 계정이 활성화되어 있는지 여부
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
}
