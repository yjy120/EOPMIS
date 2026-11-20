package com.eopmis.security;

import com.eopmis.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security 用户对象
 *   角色用 ROLE_ 前缀，配合 @PreAuthorize("hasRole('ADMIN')") 使用
 */
@Data
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private SysUser user;
    private String roleCode;
    private java.util.List<String> permissions;

    public Long getUserId() { return user.getId(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roleCode == null) return Collections.emptyList();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getUsername(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.getStatus() != null && user.getStatus() == 1; }
}
