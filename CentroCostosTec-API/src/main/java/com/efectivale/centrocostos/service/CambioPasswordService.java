package com.efectivale.centrocostos.service;

import com.efectivale.centrocostos.dto.CambioPasswordDto;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CambioPasswordService {

    private final @Qualifier("dbdespensaJdbc") JdbcTemplate dbdespensaJdbc;

    @Transactional
    public String cambiarPassword(CambioPasswordDto dto) {
        String hashGuardado = dbdespensaJdbc.query(
                "SELECT usuariopwd FROM corpusuarios WHERE usuarioid = ?",
                rs -> rs.next() ? rs.getString("usuariopwd") : null,
                dto.getId()
        );

        if (hashGuardado == null || hashGuardado.isBlank()) {
            throw new IllegalArgumentException("Usuario no Encontrado");
        }

        String actual = normalizar(AuthService.toMd5(dto.getPasswordanterior()));
        String enBd = normalizar(hashGuardado);
        if (!coincideHash(actual, enBd)) {
            throw new IllegalArgumentException("Contrasena Anterior incorrecta");
        }

        int updated = dbdespensaJdbc.update(
            "UPDATE corpusuarios SET usuariopwd = ?, usuariofechaexpirapwd = NOW(), usuariofechamodificacion = NOW(), requiere_cambio_password = FALSE WHERE usuarioid = ?",
                AuthService.toMd5(dto.getNuevoPassword()),
                dto.getId()
        );

        if (updated < 1) {
            throw new IllegalStateException("No fue posible actualizar la contrasena");
        }

        return "Contrasena Actualizada";
    }

    private boolean coincideHash(String calculado, String guardado) {
        if (calculado.equals(guardado)) {
            return true;
        }
        return calculado.replace("0", "").equals(guardado.replace("0", ""));
    }

    private String normalizar(String hash) {
        return hash == null ? "" : hash.trim().toLowerCase(Locale.ROOT);
    }
}

