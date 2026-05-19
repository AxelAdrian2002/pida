package com.efectivale.centrocostos.service;

import com.efectivale.centrocostos.dto.LoginDto;
import com.efectivale.centrocostos.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Servicio de autenticaciÃ³n alineado con el flujo de ApiCentroCostos ().
 *
 * Flujo:
 *  1. Valida corporativo en tabla 'corporativos' (dbdespensa)
 *  2. Resuelve clienteid / consignatarioid activos para ese corporativo
 *  3. Valida usuario en tabla 'corpusuarios' (dbdespensa) con contrasena MD5
 *  4. Genera JWT con claims del usuario
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** JdbcTemplate apuntando a la BD primaria dbdespensa (PostgreSQL). */
    private final JdbcTemplate primaryJdbc;
    private final JwtUtil jwtUtil;
    private final TenantPermissionService tenantPermissionService;

    public AuthService(@Qualifier("dbdespensaJdbc") JdbcTemplate primaryJdbc,
                       JwtUtil jwtUtil,
                       TenantPermissionService tenantPermissionService) {
        this.primaryJdbc = primaryJdbc;
        this.jwtUtil = jwtUtil;
        this.tenantPermissionService = tenantPermissionService;
    }

    // -----------------------------------------------------------------------
    public Map<String, Object> login(LoginDto dto) {
        log.info("Iniciando autenticacion para usuario '{}' corp='{}'",
            dto.getUsername(), dto.getCorporativo());

        // 1. Validar corporativo
        validarCorporativo(dto.getCorporativo());

        // 2. Resolver contexto operativo principal del corporativo
        long[] clienteConsignatario = obtenerContextoOperativo(dto.getCorporativo());
        long clienteId       = clienteConsignatario[0];
        long consignatarioId = clienteConsignatario[1];

        // 3. Validar usuario y contrasena (MD5)
        Integer intentosDto = dto.getIntentos();
        int intentos = intentosDto != null ? intentosDto : 0;
        Map<String, Object> usuarioData = validarUsuario(dto.getCorporativo(),
                dto.getUsername(), dto.getPassword(), intentos);

        int perfilId  = ((Number) usuarioData.get("perfilid")).intValue();
        int usuarioId = ((Number) usuarioData.get("usuarioid")).intValue();
        String nombre = (String) usuarioData.getOrDefault("usuarionombre", "");
        String centroId = (String) usuarioData.getOrDefault("centroid", "");
        boolean requiereCambioPassword = parseBoolean(usuarioData.get("requiere_cambio_password"));
        String rol    = mapearRol(perfilId);
        List<String> permisos = tenantPermissionService.resolvePermissions(dto.getCorporativo(), usuarioId, perfilId)
            .stream()
            .sorted()
            .toList();

        // 4. Generar JWT con claims extendidos
        String token = jwtUtil.generateToken(dto.getUsername(), rol, usuarioId, clienteId, consignatarioId,
            dto.getCorporativo(), centroId, permisos);

        log.info("Autenticacion exitosa para usuario '{}' rol='{}' clienteId={}", dto.getUsername(), rol, clienteId);

        return Map.ofEntries(
            Map.entry("token", token),
            Map.entry("username", dto.getUsername()),
            Map.entry("nombreCompleto", nombre != null ? nombre : ""),
            Map.entry("rol", rol),
            Map.entry("idUsuario", usuarioId),
            Map.entry("clienteId", clienteId),
            Map.entry("consignatarioId", consignatarioId),
            Map.entry("corporativoId", dto.getCorporativo()),
            Map.entry("centroId", centroId),
            Map.entry("permisos", permisos),
            Map.entry("requiereCambioPassword", requiereCambioPassword)
        );
    }

    // -----------------------------------------------------------------------
    // Validaciones privadas (espejo de LoginImplement.java del )
    // -----------------------------------------------------------------------

    private void validarCorporativo(String corporativoId) {
        try {
            SqlRowSet rs = primaryJdbc.queryForRowSet(
                "SELECT corporativoid FROM corporativos WHERE corporativoactivo = true AND corporativoID = ?",
                corporativoId);
            if (!rs.next()) {
                throw new IllegalArgumentException("No existe el Corporativo o no estÃ¡ activo");
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw e;
            log.error("Error validando corporativo: {}", e.getMessage());
            throw new IllegalArgumentException("Error al validar corporativo: " + e.getMessage());
        }
    }

    private long[] obtenerContextoOperativo(String corporativoId) {
        try {
            SqlRowSet rs = primaryJdbc.queryForRowSet(
                "SELECT clienteid, consignatarioid FROM centrocostos " +
                "WHERE corporativoid = ? AND centroactivo = true " +
                "ORDER BY centroid LIMIT 1",
                corporativoId);
            if (!rs.next()) {
                throw new IllegalArgumentException("No existe una unidad operativa activa para la empresa");
            }
            return new long[]{ rs.getLong("clienteid"), rs.getLong("consignatarioid") };
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw e;
            log.error("Error validando contexto operativo: {}", e.getMessage());
            throw new IllegalArgumentException("Error al validar el contexto operativo: " + e.getMessage());
        }
    }

    private Map<String, Object> validarUsuario(String corporativoId,
                                               String username, String password, int intentos) {
        try {
            SqlRowSet rs = primaryJdbc.queryForRowSet(
                "SELECT u.usuarioid, u.usuarionombre, u.usuariopwd, u.usuarioactivo, u.perfilid, " +
                "       u.centroid, u.requiere_cambio_password, u.email_verificado " +
                "FROM corpusuarios u " +
                "WHERE u.corporativoID = ? AND u.usuarioUSR = ?",
                corporativoId, username);

            if (!rs.next()) {
                throw new IllegalArgumentException("No existe el usuario");
            }

            int usuarioId = rs.getInt("usuarioid");
            boolean activo = rs.getBoolean("usuarioactivo");
            boolean emailVerificado = rs.getBoolean("email_verificado");

            String pwdBd  = normalizarHash(rs.getString("usuariopwd"));
            String pwdMd5 = normalizarHash(toMd5(password));

            if (!coincidePassword(pwdMd5, pwdBd)) {
                if (intentos >= 3) {
                    bloquearUsuario(corporativoId, usuarioId);
                    throw new IllegalArgumentException("Intentos de acceso excedidos. Usuario bloqueado");
                }
                throw new IllegalArgumentException("Contrasena incorrecta");
            }

            if (!emailVerificado) {
                throw new IllegalArgumentException("La cuenta aun no esta verificada. Revisa tu correo.");
            }

            if (!activo) {
                reactivarUsuario(corporativoId, usuarioId);
            }

            return Map.of(
                "usuarioid",   usuarioId,
                "usuarionombre", rs.getString("usuarionombre") != null ? rs.getString("usuarionombre") : "",
                "perfilid",    rs.getInt("perfilid"),
                "centroid",    rs.getString("centroid") != null ? rs.getString("centroid") : "",
                "requiere_cambio_password", rs.getBoolean("requiere_cambio_password")
            );
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw e;
            log.error("Error validando usuario: {}", e.getMessage());
            throw new IllegalArgumentException("Error al validar usuario: " + e.getMessage());
        }
    }

    private void bloquearUsuario(String corporativoId, int usuarioId) {
        try {
            primaryJdbc.update(
                "UPDATE corpusuarios SET usuarioActivo = FALSE, usuariofechamodificacion = NOW() " +
                "WHERE corporativoid = ? AND usuarioId = ? AND usuarioActivo IS TRUE",
                corporativoId, usuarioId);
            log.warn("Usuario {} bloqueado por exceder intentos de login", usuarioId);
        } catch (Exception ex) {
            log.error("Error bloqueando usuario {}: {}", usuarioId, ex.getMessage());
        }
    }

    private void reactivarUsuario(String corporativoId, int usuarioId) {
        try {
            primaryJdbc.update(
                "UPDATE corpusuarios SET usuarioActivo = TRUE, usuariofechamodificacion = NOW() " +
                "WHERE corporativoid = ? AND usuarioId = ? AND usuarioActivo IS FALSE",
                corporativoId, usuarioId);
            log.info("Usuario {} reactivado tras autenticacion valida", usuarioId);
        } catch (Exception ex) {
            log.error("Error reactivando usuario {}: {}", usuarioId, ex.getMessage());
        }
    }

    private String normalizarHash(String hash) {
        return hash == null ? "" : hash.trim().toLowerCase(Locale.ROOT);
    }

    private boolean coincidePassword(String hashCalculado, String hashBd) {
        if (hashCalculado.equals(hashBd)) {
            return true;
        }
        return hashCalculado.replace("0", "").equals(hashBd.replace("0", ""));
    }

    private boolean parseBoolean(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value == null) {
            return false;
        }
        return "true".equalsIgnoreCase(String.valueOf(value).trim());
    }

    // -----------------------------------------------------------------------
    // Utilidades
    // -----------------------------------------------------------------------

    private String mapearRol(int perfilId) {
        return switch (perfilId) {
            case 1, 4 -> "ADMIN";
            case 2 -> "CAPTURA";
            case 5 -> "AUTORIZADOR";
            case 3 -> "CONSULTA";
            default -> "CONSULTA";
        };
    }

    public static String toMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generando MD5", e);
        }
    }
}


