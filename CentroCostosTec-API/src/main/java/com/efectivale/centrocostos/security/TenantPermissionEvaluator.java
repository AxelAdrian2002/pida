package com.efectivale.centrocostos.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

@Component("perm")
public class TenantPermissionEvaluator {

    public boolean has(String permiso) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || permiso == null || permiso.isBlank()) {
            return false;
        }

        String authority = "PERM_" + permiso.trim().toUpperCase(Locale.ROOT);
        return auth.getAuthorities().stream()
            .anyMatch(a -> authority.equals(a.getAuthority()));
    }

    public boolean any(String... permisos) {
        return Arrays.stream(permisos).anyMatch(this::has);
    }
}
