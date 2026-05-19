package com.efectivale.centrocostos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                String rol = jwtUtil.getRolFromToken(token);
                
                // Extraer datos de contexto del JWT
                Long idUsuario = jwtUtil.getIdUsuarioFromToken(token);
                Long clienteId = jwtUtil.getClienteIdFromToken(token);
                Long consignatarioId = jwtUtil.getConsignatarioIdFromToken(token);
                String corporativoId = jwtUtil.getCorporativoIdFromToken(token);
                String centroId = jwtUtil.getCentroIdFromToken(token);
                List<String> permisos = jwtUtil.getPermisosFromToken(token);
                
                // Crear objeto de detalles del contexto
                ContextDetails contextDetails = new ContextDetails(
                    idUsuario, clienteId, consignatarioId, corporativoId, centroId
                );
                
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + rol));
                for (String permiso : permisos) {
                    String normalized = permiso == null ? "" : permiso.trim().toUpperCase(Locale.ROOT);
                    if (!normalized.isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority("PERM_" + normalized));
                    }
                }

                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                auth.setDetails(contextDetails);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
