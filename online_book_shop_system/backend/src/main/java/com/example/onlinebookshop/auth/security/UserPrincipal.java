package com.example.onlinebookshop.auth.security;

import com.example.onlinebookshop.Entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // chống null để không 500
        if (user == null || user.getRole() == null || user.getRole().getCode() == null) {
            // nếu role lỗi thì coi như không có quyền gì -> login xong sẽ bị chặn route staff
            return List.of();
        }

        String roleCode = user.getRole().getCode().trim().toUpperCase(); // STAFF / ADMIN / ...
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    @Override
    public String getPassword() {
        // nếu null -> Spring Security có thể nổ khi login, nên trả "" để khỏi crash,
        // nhưng bạn vẫn phải set password_hash cho user trong DB để login thành công.
        return (user.getPasswordHash() == null) ? "" : user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        // nếu bạn có soft-delete/status -> check ở đây
        // ưu tiên không crash:
        try {
            return user != null && user.isActive();
        } catch (Exception e) {
            return true;
        }
    }
}