package com.efectivale.centrocostos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.efectivale.centrocostos.entity.Usuario;
import com.efectivale.centrocostos.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Lazy
public class DevUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevUserInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.dev-user.enabled:true}")
    private boolean enabled;

    @Value("${app.dev-user.username:admin}")
    private String username;

    @Value("${app.dev-user.password:Admin123*}")
    private String rawPassword;

    @Value("${app.dev-user.rol:ADMIN}")
    private String rol;

    @Value("${app.dev-user.nombre-completo:Administrador TEC}")
    private String nombreCompleto;

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        try {
            var existente = usuarioRepository.findFirstByUsernameOrderByActivoDescFechaUltimoAccesoDescIdUsuarioDesc(username);
            if (existente.isEmpty()) {
                log.warn("Usuario de desarrollo no encontrado en corpusuarios: {}. Se omite creacion automatica porque la tabla  requiere columnas obligatorias adicionales.", username);
                return;
            }

            Usuario usuario = existente.get();

            usuario.setUsername(username);
            usuario.setPassword(passwordEncoder.encode(rawPassword));
            usuario.setRol(rol);
            usuario.setNombreCompleto(nombreCompleto);
            usuario.setActivo(true);
            usuario.setFechaUltimoAcceso(java.time.LocalDateTime.now());

            usuarioRepository.save(usuario);
            log.info("Usuario de desarrollo actualizado: {}", username);
        } catch (Exception e) {
            log.warn("Error inicializando usuario de desarrollo: {}", e.getMessage());
        }
    }
}

