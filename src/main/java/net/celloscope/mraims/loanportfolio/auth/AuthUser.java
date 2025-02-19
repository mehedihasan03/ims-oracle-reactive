package net.celloscope.mraims.loanportfolio.auth;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
@Builder
public class AuthUser implements UserDetails {
    private String userId;
    private String userRole;
    private String schemaName;
    private String mfiId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> userRole);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userId;
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

