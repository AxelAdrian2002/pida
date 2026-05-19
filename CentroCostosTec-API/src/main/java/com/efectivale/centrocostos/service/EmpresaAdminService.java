package com.efectivale.centrocostos.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.efectivale.centrocostos.security.ContextProvider;

@Service
public class EmpresaAdminService {

    private final JdbcTemplate jdbc;
    private final ContextProvider contextProvider;
    private final CuentaVerificacionService cuentaVerificacionService;
    private final TenantAuditService tenantAuditService;

    public EmpresaAdminService(@Qualifier("pddespensaJdbc") JdbcTemplate jdbc,
                               ContextProvider contextProvider,
                               CuentaVerificacionService cuentaVerificacionService,
                               TenantAuditService tenantAuditService) {
        this.jdbc = jdbc;
        this.contextProvider = contextProvider;
        this.cuentaVerificacionService = cuentaVerificacionService;
        this.tenantAuditService = tenantAuditService;
    }

    public String generarPlantillaCsv() {
        return "nombre,apellidoPaterno,apellidoMaterno,email,telefono\n"
            + "Ana,Lopez,Ruiz,ana.lopez@empresa.com,5511002200\n"
            + "Carlos,Hernandez,Diaz,carlos.hernandez@empresa.com,5511002201\n";
    }

    public String generarPlantillaCredencialesCsv() {
        return "numeroCredencial,numeroEmpleado,estado\n"
            + "71000001,10001,ACTIVA\n"
            + "71000002,10002,INACTIVA\n";
    }

    public Map<String, Object> obtenerConfiguracion() {
        String corporativoId = contextProvider.getCorporativoId();
        Long idUsuario = contextProvider.getIdUsuario();

        Map<String, Object> empresa = jdbc.query(
            "SELECT codigo_empresa, nombre_empresa, color_primario, color_secundario, logo_url, razon_social, rfc, "
                + "email_contacto, telefono_contacto, sitio_web, calle, numero_exterior, numero_interior, colonia, municipio, estado, pais, codigo_postal "
                + "FROM empresa_operativa WHERE codigo_empresa = ?",
            rs -> {
                if (!rs.next()) {
                    return Map.of();
                }
                Map<String, Object> empresaMap = new HashMap<>();
                empresaMap.put("codigoEmpresa", nullSafe(rs.getString("codigo_empresa")));
                empresaMap.put("nombreEmpresa", nullSafe(rs.getString("nombre_empresa")));
                empresaMap.put("colorPrimario", nullSafe(rs.getString("color_primario")));
                empresaMap.put("colorSecundario", nullSafe(rs.getString("color_secundario")));
                empresaMap.put("logoUrl", nullSafe(rs.getString("logo_url")));
                empresaMap.put("razonSocial", nullSafe(rs.getString("razon_social")));
                empresaMap.put("rfc", nullSafe(rs.getString("rfc")));
                empresaMap.put("emailContacto", nullSafe(rs.getString("email_contacto")));
                empresaMap.put("telefonoContacto", nullSafe(rs.getString("telefono_contacto")));
                empresaMap.put("sitioWeb", nullSafe(rs.getString("sitio_web")));

                Map<String, String> direccion = new HashMap<>();
                direccion.put("calle", nullSafe(rs.getString("calle")));
                direccion.put("numeroExterior", nullSafe(rs.getString("numero_exterior")));
                direccion.put("numeroInterior", nullSafe(rs.getString("numero_interior")));
                direccion.put("colonia", nullSafe(rs.getString("colonia")));
                direccion.put("municipio", nullSafe(rs.getString("municipio")));
                direccion.put("estado", nullSafe(rs.getString("estado")));
                direccion.put("pais", nullSafe(rs.getString("pais")));
                direccion.put("codigoPostal", nullSafe(rs.getString("codigo_postal")));
                empresaMap.put("direccion", direccion);
                return empresaMap;
            },
            corporativoId
        );

        Map<String, Object> perfil = jdbc.query(
            "SELECT up.curp, up.rfc, up.foto_url, u.usuarionombre, u.usuariocorreo "
                + "FROM usuario_interno u "
                + "LEFT JOIN usuario_perfil up ON up.usuarioid = u.usuarioid "
                + "WHERE u.usuarioid = ?",
            rs -> {
                if (!rs.next()) {
                    return Map.of();
                }

                Map<String, Object> perfilMap = new HashMap<>();
                perfilMap.put("nombre", nullSafe(rs.getString("usuarionombre")));
                perfilMap.put("email", nullSafe(rs.getString("usuariocorreo")));
                perfilMap.put("curp", nullSafe(rs.getString("curp")));
                perfilMap.put("rfc", nullSafe(rs.getString("rfc")));
                perfilMap.put("fotoUrl", nullSafe(rs.getString("foto_url")));
                return perfilMap;
            },
            idUsuario
        );

        Map<String, Object> out = new HashMap<>();
        out.put("empresa", empresa);
        out.put("perfil", perfil);
        return out;
    }

    @Transactional
    public Map<String, Object> guardarConfiguracion(Map<String, Object> payload) {
        String corporativoId = contextProvider.getCorporativoId();
        Long idUsuario = contextProvider.getIdUsuario();

        Map<String, Object> empresa = castMap(payload.get("empresa"));
        Map<String, Object> perfil = castMap(payload.get("perfil"));

        if (!empresa.isEmpty()) {
            jdbc.update(
                "UPDATE empresa_operativa SET color_primario = ?, color_secundario = ?, logo_url = ?, razon_social = ?, rfc = ?, "
                    + "email_contacto = ?, telefono_contacto = ?, sitio_web = ?, calle = ?, numero_exterior = ?, numero_interior = ?, "
                    + "colonia = ?, municipio = ?, estado = ?, pais = ?, codigo_postal = ?, fecha_modificacion = NOW() "
                    + "WHERE codigo_empresa = ?",
                toStr(empresa.get("colorPrimario")),
                toStr(empresa.get("colorSecundario")),
                toStr(empresa.get("logoUrl")),
                toStr(empresa.get("razonSocial")),
                toStr(empresa.get("rfc")),
                toStr(empresa.get("emailContacto")),
                toStr(empresa.get("telefonoContacto")),
                toStr(empresa.get("sitioWeb")),
                toStr(castMap(empresa.get("direccion")).get("calle")),
                toStr(castMap(empresa.get("direccion")).get("numeroExterior")),
                toStr(castMap(empresa.get("direccion")).get("numeroInterior")),
                toStr(castMap(empresa.get("direccion")).get("colonia")),
                toStr(castMap(empresa.get("direccion")).get("municipio")),
                toStr(castMap(empresa.get("direccion")).get("estado")),
                toStr(castMap(empresa.get("direccion")).get("pais")),
                toStr(castMap(empresa.get("direccion")).get("codigoPostal")),
                corporativoId
            );
        }

        if (!perfil.isEmpty()) {
            jdbc.update(
                "INSERT INTO usuario_perfil (usuarioid, curp, rfc, foto_url, fecha_modificacion) VALUES (?, ?, ?, ?, NOW()) "
                    + "ON CONFLICT (usuarioid) DO UPDATE SET curp = EXCLUDED.curp, rfc = EXCLUDED.rfc, "
                    + "foto_url = EXCLUDED.foto_url, fecha_modificacion = NOW()",
                idUsuario,
                toStr(perfil.get("curp")),
                toStr(perfil.get("rfc")),
                toStr(perfil.get("fotoUrl"))
            );
        }

        tenantAuditService.logFromContext(
            "EMPRESA",
            "GUARDAR_CONFIGURACION",
            "Configuracion de empresa actualizada",
            contextProvider
        );

        return obtenerConfiguracion();
    }

    @Transactional
    public Map<String, Object> cargarEmpleados(MultipartFile archivoCsv) {
        if (archivoCsv == null || archivoCsv.isEmpty()) {
            throw new IllegalArgumentException("Debes adjuntar un archivo CSV con la plantilla de empleados");
        }

        String corporativoId = contextProvider.getCorporativoId();
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();

        int insertados = 0;
        int actualizados = 0;
        int rechazados = 0;
        int usuariosGenerados = 0;
        int credencialesAsignadas = 0;
        int credencialesGeneradas = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(archivoCsv.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) {
                    continue;
                }
                if (row == 1 && line.toLowerCase().contains("nombre")) {
                    continue;
                }

                String[] cols = line.split(",", -1);
                if (cols.length < 2) {
                    rechazados++;
                    continue;
                }

                String nombre = cols[0].trim();
                String apellidoPaterno = cols[1].trim();
                String apellidoMaterno = cols.length > 2 ? cols[2].trim() : "";
                String email = cols.length > 3 ? cols[3].trim() : "";
                String telefono = cols.length > 4 ? cols[4].trim() : "";

                if (nombre.isBlank() || apellidoPaterno.isBlank()) {
                    rechazados++;
                    continue;
                }

                String numeroEmpleado = jdbc.queryForObject(
                    "WITH next_id AS ( "
                        + "SELECT nextval(pg_get_serial_sequence('colaborador','colaborador_id')) AS id "
                    + ") "
                    + "INSERT INTO colaborador (colaborador_id, tnuec, tnoem, tappa, tapma, tmail, ttele, tnucl, tnuco, tgrup, tbist, tbife) "
                    + "SELECT id, LPAD(id::text, 5, '0'), ?, ?, ?, ?, ?, ?, ?, ?, 'A', NOW() "
                    + "FROM next_id RETURNING tnuec",
                    String.class,
                    nombre,
                    apellidoPaterno,
                    apellidoMaterno,
                    email,
                    telefono,
                    clienteId,
                    consignatarioId,
                    "SIN_GRUPO"
                );

                if (numeroEmpleado == null || numeroEmpleado.isBlank()) {
                    rechazados++;
                    continue;
                }

                insertados++;

                if (garantizarUsuarioEmpleado(corporativoId, numeroEmpleado, nombre, apellidoPaterno, email)) {
                    usuariosGenerados++;
                }

                String nombreCompleto = (nombre + " " + apellidoPaterno + " " + apellidoMaterno).trim().replaceAll("\\s+", " ");
                Map<String, Object> asignacionCredencial = asegurarCredencialParaEmpleado(
                    numeroEmpleado,
                    nombreCompleto,
                    clienteId,
                    consignatarioId
                );
                credencialesAsignadas += toInt(asignacionCredencial.get("asignadas"));
                credencialesGeneradas += toInt(asignacionCredencial.get("generadas"));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer el archivo de empleados", ex);
        }

        tenantAuditService.logFromContext(
            "EMPLEADOS",
            "CARGA_MASIVA",
            "Carga masiva completada insertados=" + insertados +
                " actualizados=" + actualizados +
                " rechazados=" + rechazados +
                " usuariosGenerados=" + usuariosGenerados +
                " credencialesAsignadas=" + credencialesAsignadas +
                " credencialesGeneradas=" + credencialesGeneradas,
            contextProvider
        );

        return Map.of(
            "insertados", insertados,
            "actualizados", actualizados,
            "rechazados", rechazados,
            "usuariosGenerados", usuariosGenerados,
            "credencialesAsignadas", credencialesAsignadas,
            "credencialesGeneradas", credencialesGeneradas
        );
    }

    @Transactional
    public Map<String, Object> cargarCredenciales(MultipartFile archivoCsv) {
        if (archivoCsv == null || archivoCsv.isEmpty()) {
            throw new IllegalArgumentException("Debes adjuntar un archivo CSV con la plantilla de credenciales");
        }

        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();

        int insertados = 0;
        int actualizados = 0;
        int rechazados = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(archivoCsv.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) {
                    continue;
                }
                if (row == 1 && line.toLowerCase(Locale.ROOT).contains("numerocredencial")) {
                    continue;
                }

                String[] cols = line.split(",", -1);
                if (cols.length < 1) {
                    rechazados++;
                    continue;
                }

                Long numeroCredencial = parseLong(cols[0].trim());
                String numeroEmpleado = cols.length > 1 ? cols[1].trim() : "";
                String estado = cols.length > 2 ? cols[2].trim() : "INACTIVA";

                if (numeroCredencial == null || numeroCredencial <= 0) {
                    rechazados++;
                    continue;
                }

                String estadoNormalizado = "ACTIVA".equalsIgnoreCase(estado) ? "ACTIVA" : "INACTIVA";

                String empleadoId = numeroEmpleado.isBlank() ? "SIN_ASIGNAR" : numeroEmpleado;
                String empleadoNombre = resolverNombreEmpleado(numeroEmpleado, clienteId, consignatarioId);
                if (empleadoNombre == null) {
                    if (!numeroEmpleado.isBlank()) {
                        rechazados++;
                        continue;
                    }
                    empleadoNombre = "SIN ASIGNAR";
                }

                Map<String, Object> existente = jdbc.query(
                    "SELECT tarjetaid, clienteid, consignatarioid FROM credencial_interna WHERE tarjetaid = ?",
                    rs -> {
                        if (!rs.next()) {
                            return null;
                        }
                        Map<String, Object> m = new HashMap<>();
                        m.put("clienteid", rs.getLong("clienteid"));
                        m.put("consignatarioid", rs.getLong("consignatarioid"));
                        return m;
                    },
                    numeroCredencial
                );

                boolean activa = "ACTIVA".equals(estadoNormalizado);

                if (existente == null) {
                    Long parametrosId = jdbc.queryForObject(
                        "INSERT INTO estado_credencial (parametrosactiva) VALUES (?) RETURNING parametrosid",
                        Long.class,
                        activa
                    );

                    jdbc.update(
                        "INSERT INTO credencial_interna (tarjetaid, empleadoid, empleadonombre, cuentaid, parametrosid, tarjetatipo, clienteid, consignatarioid, tarjetafechacreacion, tarjetafechamodificacion, tarjetacancelada) "
                            + "VALUES (?, ?, ?, ?, ?, 'T', ?, ?, NOW(), NOW(), FALSE)",
                        numeroCredencial,
                        empleadoId,
                        empleadoNombre,
                        "CTA-" + empleadoId,
                        parametrosId,
                        clienteId,
                        consignatarioId
                    );
                    insertados++;
                } else {
                    Long clienteExistente = (Long) existente.get("clienteid");
                    Long consignatarioExistente = (Long) existente.get("consignatarioid");

                    if (!clienteId.equals(clienteExistente) || !consignatarioId.equals(consignatarioExistente)) {
                        rechazados++;
                        continue;
                    }

                    jdbc.update(
                        "UPDATE credencial_interna c SET empleadoid = ?, empleadonombre = ?, cuentaid = ?, tarjetafechamodificacion = NOW() WHERE c.tarjetaid = ?",
                        empleadoId,
                        empleadoNombre,
                        "CTA-" + empleadoId,
                        numeroCredencial
                    );

                    jdbc.update(
                        "UPDATE estado_credencial ec SET parametrosactiva = ? FROM credencial_interna c WHERE c.tarjetaid = ? AND c.parametrosid = ec.parametrosid",
                        activa,
                        numeroCredencial
                    );

                    actualizados++;
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer el archivo de credenciales", ex);
        }

        tenantAuditService.logFromContext(
            "CREDENCIALES",
            "CARGA_MASIVA",
            "Carga de credenciales completada insertados=" + insertados
                + " actualizados=" + actualizados
                + " rechazados=" + rechazados,
            contextProvider
        );

        return Map.of(
            "insertados", insertados,
            "actualizados", actualizados,
            "rechazados", rechazados
        );
    }

    @Transactional
    public Map<String, Object> generarLoteInicialCredenciales(Integer cantidad, Long inicio) {
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();

        int cantidadFinal = cantidad == null ? 0 : cantidad;
        if (cantidadFinal <= 0 || cantidadFinal > 5000) {
            throw new IllegalArgumentException("La cantidad debe estar entre 1 y 5000");
        }

        long base = (inicio == null || inicio <= 0) ? siguienteNumeroCredencialDisponible() : inicio;

        int insertados = 0;
        int rechazados = 0;
        long ultimoNumero = base - 1;

        for (int i = 0; i < cantidadFinal; i++) {
            long numero = base + i;
            ultimoNumero = numero;

            Integer existe = jdbc.queryForObject(
                "SELECT COUNT(1) FROM credencial_interna WHERE tarjetaid = ?",
                Integer.class,
                numero
            );

            if (existe != null && existe > 0) {
                rechazados++;
                continue;
            }

            Long parametrosId = jdbc.queryForObject(
                "INSERT INTO estado_credencial (parametrosactiva) VALUES (FALSE) RETURNING parametrosid",
                Long.class
            );

            jdbc.update(
                "INSERT INTO credencial_interna (tarjetaid, empleadoid, empleadonombre, cuentaid, parametrosid, tarjetatipo, clienteid, consignatarioid, tarjetafechacreacion, tarjetafechamodificacion, tarjetacancelada) "
                    + "VALUES (?, 'SIN_ASIGNAR', 'SIN ASIGNAR', ?, ?, 'T', ?, ?, NOW(), NOW(), FALSE)",
                numero,
                "CTA-SIN_ASIGNAR",
                parametrosId,
                clienteId,
                consignatarioId
            );
            insertados++;
        }

        tenantAuditService.logFromContext(
            "CREDENCIALES",
            "GENERAR_LOTE_INICIAL",
            "Lote inicial generado insertados=" + insertados + " rechazados=" + rechazados + " inicio=" + base,
            contextProvider
        );

        return Map.of(
            "insertados", insertados,
            "rechazados", rechazados,
            "inicio", base,
            "fin", ultimoNumero
        );
    }

    @Transactional
    public Map<String, Object> asignarCredencialesAutomaticamente(Integer limite) {
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();

        int limiteFinal = limite == null ? 100 : limite;
        if (limiteFinal <= 0 || limiteFinal > 5000) {
            throw new IllegalArgumentException("El limite debe estar entre 1 y 5000");
        }

        java.util.List<Map<String, Object>> empleadosSinCredencial = jdbc.query(
            "SELECT c.tnuec AS numero_empleado, "
                + "TRIM(COALESCE(c.tnoem,'') || ' ' || COALESCE(c.tappa,'') || ' ' || COALESCE(c.tapma,'')) AS nombre_empleado "
                + "FROM colaborador c "
                + "LEFT JOIN credencial_interna ci ON ci.empleadoid = c.tnuec AND ci.clienteid = c.tnucl AND ci.consignatarioid = c.tnuco "
                + "WHERE c.tnucl = ? AND c.tnuco = ? AND c.tbist = 'A' AND ci.tarjetaid IS NULL "
                + "ORDER BY c.colaborador_id "
                + "LIMIT ?",
            (rs, rowNum) -> {
                Map<String, Object> m = new HashMap<>();
                m.put("numero_empleado", rs.getString("numero_empleado"));
                m.put("nombre_empleado", rs.getString("nombre_empleado"));
                return m;
            },
            clienteId,
            consignatarioId,
            limiteFinal
        );

        java.util.List<Long> stockDisponible = jdbc.query(
            "SELECT tarjetaid FROM credencial_interna "
                + "WHERE clienteid = ? AND consignatarioid = ? AND empleadoid = 'SIN_ASIGNAR' AND tarjetacancelada = FALSE "
                + "ORDER BY tarjetaid "
                + "LIMIT ?",
            (rs, rowNum) -> rs.getLong("tarjetaid"),
            clienteId,
            consignatarioId,
            limiteFinal
        );

        int asignadas = 0;
        int sinStock = 0;
        int sinEmpleado = 0;

        int totalOperable = Math.min(empleadosSinCredencial.size(), stockDisponible.size());
        for (int i = 0; i < totalOperable; i++) {
            Map<String, Object> empleado = empleadosSinCredencial.get(i);
            Long tarjetaId = stockDisponible.get(i);
            String numeroEmpleado = String.valueOf(empleado.get("numero_empleado"));
            String nombreEmpleado = String.valueOf(empleado.get("nombre_empleado"));

            jdbc.update(
                "UPDATE credencial_interna SET empleadoid = ?, empleadonombre = ?, cuentaid = ?, tarjetafechamodificacion = NOW() WHERE tarjetaid = ?",
                numeroEmpleado,
                nombreEmpleado,
                "CTA-" + numeroEmpleado,
                tarjetaId
            );

            asignadas++;
        }

        if (empleadosSinCredencial.size() > stockDisponible.size()) {
            sinStock = empleadosSinCredencial.size() - stockDisponible.size();
        }
        if (stockDisponible.size() > empleadosSinCredencial.size()) {
            sinEmpleado = stockDisponible.size() - empleadosSinCredencial.size();
        }

        tenantAuditService.logFromContext(
            "CREDENCIALES",
            "ASIGNACION_AUTOMATICA",
            "Asignacion automatica ejecutada asignadas=" + asignadas + " sinStock=" + sinStock + " sinEmpleado=" + sinEmpleado,
            contextProvider
        );

        return Map.of(
            "asignadas", asignadas,
            "sinStock", sinStock,
            "sinEmpleado", sinEmpleado,
            "candidatosEmpleados", empleadosSinCredencial.size(),
            "stockDisponible", stockDisponible.size()
        );
    }

    private boolean garantizarUsuarioEmpleado(String corporativoId,
                                              String numeroEmpleado,
                                              String nombre,
                                              String apellidoPaterno,
                                              String email) {
        String username = "emp" + numeroEmpleado;
        Integer exists = jdbc.queryForObject(
            "SELECT COUNT(1) FROM corpusuarios WHERE corporativoid = ? AND usuariousr = ?",
            Integer.class,
            corporativoId,
            username
        );

        if (exists != null && exists > 0) {
            return false;
        }

        String centroId = jdbc.query(
            "SELECT codigo_unidad FROM unidad_operativa WHERE codigo_empresa = ? AND activa = TRUE ORDER BY codigo_unidad LIMIT 1",
            rs -> rs.next() ? rs.getString("codigo_unidad") : null,
            corporativoId
        );

        if (centroId == null || centroId.isBlank()) {
            throw new IllegalStateException("No existe una unidad operativa activa para crear usuarios en la empresa " + corporativoId);
        }

        String nombreCompleto = (nombre + " " + apellidoPaterno).trim();
        String correo = (email == null || email.isBlank()) ? (username + "@empresa.local") : email;

        Long usuarioId = jdbc.queryForObject(
            "INSERT INTO usuario_interno (usuariousr, usuarionombre, usuariopwd, usuariocorreo, usuarioactivo, perfilid, corporativoid, centroid, "
                + "usuariofechacreacion, usuariofechamodificacion, usuariofechaexpirapwd, bitacoraid, requiere_cambio_password, email_verificado) "
                + "VALUES (?, ?, ?, ?, TRUE, 3, ?, ?, NOW(), NOW(), NOW(), 0, TRUE, TRUE) RETURNING usuarioid",
            Long.class,
            username,
            nombreCompleto,
            AuthService.toMd5("demo"),
            correo,
            corporativoId,
            centroId
        );

        if (usuarioId == null) {
            throw new IllegalStateException("No fue posible crear usuario para empleado " + numeroEmpleado);
        }

        cuentaVerificacionService.enviarCredencialesAcceso(correo, username, "demo", corporativoId);
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object val) {
        if (val instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return Map.of();
    }

    private String toStr(Object val) {
        return val == null ? null : String.valueOf(val).trim();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String resolverNombreEmpleado(String numeroEmpleado, Long clienteId, Long consignatarioId) {
        if (numeroEmpleado == null || numeroEmpleado.isBlank()) {
            return null;
        }

        return jdbc.query(
            "SELECT TRIM(COALESCE(tnoem,'') || ' ' || COALESCE(tappa,'') || ' ' || COALESCE(tapma,'')) AS nombre "
                + "FROM colaborador WHERE tnuec = ? AND tnucl = ? AND tnuco = ?",
            rs -> rs.next() ? rs.getString("nombre") : null,
            numeroEmpleado,
            clienteId,
            consignatarioId
        );
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private long siguienteNumeroCredencialDisponible() {
        Long max = jdbc.queryForObject(
            "SELECT COALESCE(MAX(tarjetaid), 70000000) FROM credencial_interna",
            Long.class
        );
        return (max == null ? 70000000L : max) + 1L;
    }

    private Map<String, Object> asegurarCredencialParaEmpleado(String numeroEmpleado,
                                                               String nombreEmpleado,
                                                               Long clienteId,
                                                               Long consignatarioId) {
        Integer yaTiene = jdbc.queryForObject(
            "SELECT COUNT(1) FROM credencial_interna WHERE empleadoid = ? AND clienteid = ? AND consignatarioid = ? AND tarjetacancelada = FALSE",
            Integer.class,
            numeroEmpleado,
            clienteId,
            consignatarioId
        );

        if (yaTiene != null && yaTiene > 0) {
            return Map.of("asignadas", 0, "generadas", 0);
        }

        Long tarjetaDisponible = jdbc.query(
            "SELECT tarjetaid FROM credencial_interna WHERE clienteid = ? AND consignatarioid = ? "
                + "AND empleadoid = 'SIN_ASIGNAR' AND tarjetacancelada = FALSE ORDER BY tarjetaid LIMIT 1",
            rs -> rs.next() ? rs.getLong("tarjetaid") : null,
            clienteId,
            consignatarioId
        );

        if (tarjetaDisponible != null) {
            jdbc.update(
                "UPDATE credencial_interna SET empleadoid = ?, empleadonombre = ?, cuentaid = ?, tarjetafechamodificacion = NOW() WHERE tarjetaid = ?",
                numeroEmpleado,
                nombreEmpleado,
                "CTA-" + numeroEmpleado,
                tarjetaDisponible
            );
            return Map.of("asignadas", 1, "generadas", 0);
        }

        long numeroCredencial = siguienteNumeroCredencialDisponible();
        Long parametrosId = jdbc.queryForObject(
            "INSERT INTO estado_credencial (parametrosactiva) VALUES (FALSE) RETURNING parametrosid",
            Long.class
        );

        jdbc.update(
            "INSERT INTO credencial_interna (tarjetaid, empleadoid, empleadonombre, cuentaid, parametrosid, tarjetatipo, clienteid, consignatarioid, tarjetafechacreacion, tarjetafechamodificacion, tarjetacancelada) "
                + "VALUES (?, ?, ?, ?, ?, 'T', ?, ?, NOW(), NOW(), FALSE)",
            numeroCredencial,
            numeroEmpleado,
            nombreEmpleado,
            "CTA-" + numeroEmpleado,
            parametrosId,
            clienteId,
            consignatarioId
        );

        return Map.of("asignadas", 1, "generadas", 1);
    }

    @Transactional
    public Map<String, Object> asignarCredencialManualmente(String numeroCredencial, String numeroEmpleado) {
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();
        
        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de la sesión");
        }
        
        // Validar que la credencial existe y está disponible
        Map<String, Object> credencial = jdbc.queryForMap(
            "SELECT tarjetaid, empleadoid, empleadonombre FROM credencial_interna "
                + "WHERE CAST(tarjetaid AS VARCHAR) = ? AND clienteid = ? AND consignatarioid = ?",
            numeroCredencial, clienteId, consignatarioId
        );
        
        String estadoActual = (String) credencial.get("empleadoid");
        if (!"SIN_ASIGNAR".equals(estadoActual)) {
            throw new IllegalArgumentException("La credencial " + numeroCredencial + " ya está asignada a " + credencial.get("empleadonombre"));
        }
        Long tarjetaId = ((Number) credencial.get("tarjetaid")).longValue();
        
        // Validar que el empleado existe
        Map<String, Object> empleado = jdbc.queryForMap(
            "SELECT tnuec, tnoem, tappa FROM colaborador "
                + "WHERE tnuec = ? AND tnucl = ? AND tnuco = ? AND tbist = 'A'",
            numeroEmpleado, clienteId, consignatarioId
        );
        
        String nombre = (String) empleado.get("tnoem");
        String apellido = (String) empleado.get("tappa");
        String nombreEmpleado = ((nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "")).trim();
        
        // Asignar la credencial al empleado
        jdbc.update(
            "UPDATE credencial_interna SET empleadoid = ?, empleadonombre = ?, cuentaid = ?, tarjetafechamodificacion = NOW() WHERE tarjetaid = ?",
            numeroEmpleado,
            nombreEmpleado,
            "CTA-" + numeroEmpleado,
            tarjetaId
        );
        
        // Registrar en bitácora
        tenantAuditService.logFromContext(
            "CREDENCIALES",
            "ASIGNACION_MANUAL",
            "Credencial " + numeroCredencial + " asignada manualmente a empleado " + numeroEmpleado + " (" + nombreEmpleado + ")",
            contextProvider
        );
        
        return Map.of(
            "exito", true,
            "mensaje", "Credencial asignada exitosamente",
            "numeroCredencial", numeroCredencial,
            "numeroEmpleado", numeroEmpleado,
            "nombreEmpleado", nombreEmpleado
        );
    }

    public java.util.List<Map<String, Object>> obtenerEmpleadosActivos() {
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();

        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de la sesión");
        }

        return jdbc.query(
            "SELECT c.tnuec AS numero_empleado, "
                + "TRIM(COALESCE(c.tnoem,'') || ' ' || COALESCE(c.tappa,'') || ' ' || COALESCE(c.tapma,'')) AS nombre_empleado, "
                + "COUNT(ci.tarjetaid) AS tarjetas_asignadas "
                + "FROM colaborador c "
                + "LEFT JOIN credencial_interna ci ON ci.empleadoid = c.tnuec AND ci.clienteid = c.tnucl AND ci.consignatarioid = c.tnuco AND ci.tarjetacancelada = FALSE "
                + "WHERE c.tnucl = ? AND c.tnuco = ? AND c.tbist = 'A' "
                + "GROUP BY c.tnuec, c.tnoem, c.tappa, c.tapma "
                + "ORDER BY c.tnuec",
            (rs, rowNum) -> {
                Map<String, Object> m = new HashMap<>();
                m.put("numero_empleado", rs.getString("numero_empleado"));
                m.put("nombre_empleado", rs.getString("nombre_empleado"));
                m.put("tarjetas_asignadas", rs.getInt("tarjetas_asignadas"));
                return m;
            },
            clienteId,
            consignatarioId
        );
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            return 0;
        }
    }
}
