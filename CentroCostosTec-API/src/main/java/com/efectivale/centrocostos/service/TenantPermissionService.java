package com.efectivale.centrocostos.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class TenantPermissionService {

    private final JdbcTemplate jdbcTemplate;

    public TenantPermissionService(@Qualifier("dbdespensaJdbc") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Set<String> resolvePermissions(String corporativoId, long usuarioId, int perfilId) {
        List<String> explicitPermissions = jdbcTemplate.query(
            "SELECT permiso FROM tenant_permiso_usuario "
                + "WHERE corporativoid = ? AND usuarioid = ? AND permitido = TRUE",
            (rs, rowNum) -> normalize(rs.getString("permiso")),
            corporativoId,
            usuarioId
        );

        if (!explicitPermissions.isEmpty()) {
            return new LinkedHashSet<>(explicitPermissions);
        }

        return defaultPermissionsByProfile(perfilId);
    }

    private Set<String> defaultPermissionsByProfile(int perfilId) {
        return switch (perfilId) {
            case 1, 4 -> new LinkedHashSet<>(List.of(
                "EMPRESA_CONFIG_VER",
                "EMPRESA_CONFIG_EDITAR",
                "EMPLEADOS_IMPORTAR",
                "GRUPOS_GESTIONAR",
                "GRUPOS_ASIGNAR",
                "GRUPOS_REPORTE",
                "CREDENCIALES_OPERAR",
                "SOLICITUDES_AUTORIZAR"
            ));
            case 2 -> new LinkedHashSet<>(List.of(
                "GRUPOS_GESTIONAR",
                "GRUPOS_ASIGNAR",
                "GRUPOS_REPORTE",
                "CREDENCIALES_OPERAR"
            ));
            case 5 -> new LinkedHashSet<>(List.of(
                "SOLICITUDES_AUTORIZAR",
                "GRUPOS_REPORTE"
            ));
            default -> new LinkedHashSet<>(List.of(
                "GRUPOS_REPORTE"
            ));
        };
    }

    private String normalize(String permission) {
        if (permission == null) {
            return "";
        }
        return permission.trim().toUpperCase(Locale.ROOT);
    }
}
