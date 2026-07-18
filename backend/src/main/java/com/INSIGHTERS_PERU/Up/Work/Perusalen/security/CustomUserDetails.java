package com.INSIGHTERS_PERU.Up.Work.Perusalen.security;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    // Devuelve los roles/permisos del usuario (usamos esEmpleado/esEmpleador)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (usuario.isEsEmpleado()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_EMPLEADO"));
        }
        if (usuario.isEsEmpleador()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_EMPLEADOR"));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    // Cuenta no expirada, no bloqueada, habilitada (para MVP siempre true)
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}