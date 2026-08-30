package com.javalive.backend.security;

import com.javalive.backend.entity.Admin;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Wraps the {@link Admin} entity for Spring Security — the separate "admin" guard. */
public class AdminPrincipal implements UserDetails {

    private final Admin admin;

    public AdminPrincipal(Admin admin) {
        this.admin = admin;
    }

    public Admin getAdmin() {
        return admin;
    }

    public Long getId() {
        return admin.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Business-level role (Super Admin / Admin / Rentention Agent / Conversion Agent) is
        // enforced in service methods against admin.getType(); Spring Security only needs to know
        // this principal belongs to the admin guard, not which guard-role subtype it is.
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public String getPassword() {
        return admin.getPassword();
    }

    @Override
    public String getUsername() {
        return admin.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return "active".equalsIgnoreCase(admin.getStatus());
    }
}
