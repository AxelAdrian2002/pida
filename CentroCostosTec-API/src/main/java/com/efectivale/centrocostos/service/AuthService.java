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
import java.util.Locale;
import java.util.Map;

/**
 * Servicio de autenticaciÃ³n alineado con el flujo de ApiCentroCostos ().
 *
 * Flujo:
 *  1. Valida corporativo en tabla 'corporativos' (dbdespensa)
 *  2. Valida centro de costos en tabla 'centrocostos' (dbdespensa) â†’ obtiene clienteid / consignatarioid
 *  3. Valida usuario en tabla 'corpusuarios' (dbdespensa) con contraseÃ±a MD5
 *  4. Genera JWT con claims del usuario
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** JdbcTemplate apuntando a la BD primaria dbdespensa (PostgreSQL). */
    private final JdbcTemplate primaryJdbc;
    private final JwtUtil jwtUtil;

    public AuthService(@Qualifier("dbdespensaJdbc") JdbcTemplate primaryJdbc,
                       JwtUtil jwtUtil) {
        this.primaryJdbc = primaryJdbc;
        this.jwtUtil = jwtUtil;
    }

    // -----------------------------------------------------------------------
    public Map<String, Object> login(LoginDto dto) {
        log.info("Iniciando autenticacion para usuario '{}' corp='{}' centro='{}'",
                dto.getUsername(), dto.getCorporativo(), dto.getCentrocostos());

        // 1. Validar corporativo
        validarCorporativo(dto.getCorporativo());

        // 2. Validar centro de costos y obtener clienteid / consignatarioid
        long[] clienteConsignatario = validarCentroCostos(dto.getCorporativo(), dto.getCentrocostos());
        long clienteId       = clienteConsignatario[0];
        long consignatarioId = clienteConsignatario[1];

        // 3. Validar usuario y contraseÃ±a (MD5)
        int intentos = dto.getIntentos() != null ? dto.getIntentos() : 0;
        Map<String, Object> usuarioData = validarUsuario(dto.getCorporativo(), dto.getCentrocostos(),
                dto.getUsername(), dto.getPassword(), intentos);

        int perfilId  = ((Number) usuarioData.get("perfilid")).intValue();
        int usuarioId = ((Number) usuarioData.get("usuarioid")).intValue();
        String nombre = (String) usuarioData.getOrDefault("usuarionombre", "");
        String rol    = mapearRol(perfilId);

        // 4. Generar JWT con claims extendidos
        String token = jwtUtil.generateToken(dto.getUsername(), rol, usuarioId, clienteId, consignatarioId,
                dto.getCorporativo(), dto.getCentrocostos());

        log.info("Autenticacion exitosa para usuario '{}' rol='{}' clienteId={}", dto.getUsername(), rol, clienteId);

        return Map.of(
            "token",           token,
            "username",        dto.getUsername(),
            "nombreCompleto",  nombre != null ? nombre : "",
            "rol",             rol,
            "idUsuario",       usuarioId,
            "clienteId",       clienteId,
            "consignatarioId", consignatarioId,
            "corporativoId",   dto.getCorporativo(),
            "centroId",        dto.getCentrocostos()
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

    private long[] validarCentroCostos(String corporativoId, String centroId) {
        try {
            SqlRowSet rs = primaryJdbc.queryForRowSet(
                "SELECT clienteid, consignatarioid FROM centrocostos " +
                "WHERE centroid = ? AND corporativoid = ? AND centroactivo = true",
                centroId, corporativoId);
            if (!rs.next()) {
                throw new IllegalArgumentException("No existe el Centro de Costos o no estÃ¡ activo");
            }
            return new long[]{ rs.getLong("clienteid"), rs.getLong("consignatarioid") };
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw e;
            log.error("Error validando centro de costos: {}", e.getMessage());
            throw new IllegalArgumentException("Error al validar centro de costos: " + e.getMessage());
        }
    }

    private Map<String, Object> validarUsuario(String corporativoId, String centroId,
                                               String username, String password, int intentos) {
        try {
            SqlRowSet rs = primaryJdbc.queryForRowSet(
                "SELECT u.usuarioid, u.usuarionombre, u.usuariopwd, u.usuarioactivo, u.perfilid, " +
                "       d.clienteid, d.consignatarioid " +
                "FROM corpusuarios u " +
                "JOIN centrocostos d USING(corporativoID, centroID) " +
                "WHERE u.corporativoID = ? AND u.usuarioUSR = ?",
                corporativoId, username);

            if (!rs.next()) {
                throw new IllegalArgumentException("No existe el usuario");
            }

            int usuarioId = rs.getInt("usuarioid");
            boolean activo = rs.getBoolean("usuarioactivo");

            String pwdBd  = normalizarHash(rs.getString("usuariopwd"));
            String pwdMd5 = normalizarHash(toMd5(password));

            if (!coincidePassword(pwdMd5, pwdBd)) {
                if (intentos >= 3) {
                    bloquearUsuario(corporativoId, usuarioId);
                    throw new IllegalArgumentException("Intentos de acceso excedidos. Usuario bloqueado");
                }
                throw new IllegalArgumentException("Contrasena incorrecta");
            }

            if (!activo) {
                reactivarUsuario(corporativoId, usuarioId);
            }

            return Map.of(
                "usuarioid",   usuarioId,
                "usuarionombre", rs.getString("usuarionombre") != null ? rs.getString("usuarionombre") : "",
                "perfilid",    rs.getInt("perfilid")
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


