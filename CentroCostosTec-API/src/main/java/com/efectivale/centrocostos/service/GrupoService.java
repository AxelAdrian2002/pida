package com.efectivale.centrocostos.service;

import com.efectivale.centrocostos.dto.GrupoListadoDto;
import com.efectivale.centrocostos.dto.GrupoDto;
import com.efectivale.centrocostos.entity.Grupo;
import com.efectivale.centrocostos.entity.GrupoEmpleado;
import com.efectivale.centrocostos.repository.GrupoRepository;
import com.efectivale.centrocostos.security.ContextProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private static final Logger log = LoggerFactory.getLogger(GrupoService.class);
    private final GrupoRepository grupoRepository;
    private final @Qualifier("dbdespensaJdbc") JdbcTemplate dbdespensaJdbc;
    private final @Qualifier("pddespensaJdbc") JdbcTemplate pddespensaJdbc;
    private final MultiDbBusinessService multiDbBusinessService;
    private final ContextProvider contextProvider;

    // ------- REGISTRAR GRUPO -------
    @Transactional
    public Grupo registrarGrupo(GrupoDto dto) {
        String grupoId = resolveGrupoId(dto);
        log.info("Registrando grupo: {}", grupoId);
        multiDbBusinessService.validarContextoGrupo(dto, "REGISTRAR");
        if (grupoRepository.existsByNombre(grupoId)) {
            throw new IllegalArgumentException("Ya existe un grupo con el nombre: " + grupoId);
        }

        Long idGrupo = dbdespensaJdbc.queryForObject(
                "INSERT INTO direccionesgrupo (grupoid, descripcion, estatus, fechacreacion, fechamodificacion, clienteid, consignatarioid) " +
                        "VALUES (?, ?, TRUE, NOW(), NOW(), ?, ?) RETURNING iddirecciones",
                Long.class,
                grupoId,
                dto.getDescripcion(),
                dto.getClienteId() != null ? dto.getClienteId().intValue() : 0,
                dto.getConsignatarioId() != null ? dto.getConsignatarioId().intValue() : 0
        );

        if (idGrupo == null) {
            throw new IllegalStateException("No fue posible registrar grupo");
        }

        dbdespensaJdbc.update(
            "INSERT INTO detalledirecciones (iddirecciones, calle, numero, colonia, codigopostal, delegacion, estado, nombre, telefono, nombre2, telefono2, horario, observacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            idGrupo,
            dto.getCalle(),
            dto.getNumero(),
            dto.getColonia(),
            dto.getCodigopostal(),
            dto.getDelegacion(),
            dto.getEstado(),
            dto.getNombreContacto(),
            dto.getTelefono(),
            dto.getNombre2(),
            dto.getTelefono2(),
            dto.getHorario(),
            dto.getObservacion()
        );

        return grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar el grupo creado con id " + idGrupo));
    }

    @Transactional
    public Grupo actualizarGrupo(Long idGrupo, GrupoDto dto) {
        log.info("Actualizando grupo {}", idGrupo);
        multiDbBusinessService.validarContextoGrupo(dto, "ACTUALIZAR");

        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));

        if (!grupo.getClienteId().equals(dto.getClienteId()) || !grupo.getConsignatarioId().equals(dto.getConsignatarioId())) {
            throw new IllegalArgumentException("No tiene permisos para actualizar este grupo");
        }

        String nombreNuevo = resolveGrupoId(dto);
        if (grupoRepository.existsByNombreAndIdGrupoNot(nombreNuevo, idGrupo)) {
            throw new IllegalArgumentException("Ya existe un grupo con el nombre: " + nombreNuevo);
        }

        Boolean estatus = dto.getEstatusid() != null ? dto.getEstatusid() : grupo.getActivo();

        grupo.setNombre(nombreNuevo);
        grupo.setDescripcion(dto.getDescripcion());
        grupo.setActivo(estatus);
        Grupo actualizado = grupoRepository.save(grupo);

        int detUpdated = dbdespensaJdbc.update(
            "UPDATE detalledirecciones SET calle = ?, numero = ?, colonia = ?, codigopostal = ?, delegacion = ?, estado = ?, nombre = ?, telefono = ?, nombre2 = ?, telefono2 = ?, horario = ?, observacion = ? " +
                "WHERE iddirecciones = ?",
            dto.getCalle(),
            dto.getNumero(),
            dto.getColonia(),
            dto.getCodigopostal(),
            dto.getDelegacion(),
            dto.getEstado(),
            dto.getNombreContacto(),
            dto.getTelefono(),
            dto.getNombre2(),
            dto.getTelefono2(),
            dto.getHorario(),
            dto.getObservacion(),
            idGrupo
        );

        if (detUpdated < 1) {
            dbdespensaJdbc.update(
                "INSERT INTO detalledirecciones (iddirecciones, calle, numero, colonia, codigopostal, delegacion, estado, nombre, telefono, nombre2, telefono2, horario, observacion) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                idGrupo,
                dto.getCalle(),
                dto.getNumero(),
                dto.getColonia(),
                dto.getCodigopostal(),
                dto.getDelegacion(),
                dto.getEstado(),
                dto.getNombreContacto(),
                dto.getTelefono(),
                dto.getNombre2(),
                dto.getTelefono2(),
                dto.getHorario(),
                dto.getObservacion()
            );
        }

        return actualizado;
    }

    // ------- ASIGNAR EMPLEADO A GRUPO -------
    @Transactional
    public GrupoEmpleado asignarEmpleado(Long idGrupo, Long idEmpleado,
                                          String numeroEmpleado, String usuarioAsigno) {
        log.info("Asignando empleado {} al grupo {}", idEmpleado, idGrupo);
        multiDbBusinessService.validarContextoGrupo(null, null, "ASIGNAR");
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));

        String numeroEmpleadoResolvido = numeroEmpleado;
        if ((numeroEmpleadoResolvido == null || numeroEmpleadoResolvido.isBlank()) && idEmpleado != null) {
            numeroEmpleadoResolvido = String.valueOf(idEmpleado);
        }

        if (numeroEmpleadoResolvido == null || numeroEmpleadoResolvido.isBlank()) {
            throw new IllegalArgumentException("numeroEmpleado es obligatorio para asignar grupo");
        }

        Integer yaAsignado = pddespensaJdbc.queryForObject(
                "SELECT COUNT(1) FROM tmemp WHERE tnuec = ? AND tgrup = ? AND (tbist IS NULL OR tbist <> 'X')",
                Integer.class,
                numeroEmpleadoResolvido,
                grupo.getNombre()
        );
        if (yaAsignado != null && yaAsignado > 0) {
            throw new IllegalArgumentException("El empleado ya pertenece activamente a este grupo");
        }

        int updated = pddespensaJdbc.update(
                "UPDATE tmemp SET tgrup = ? WHERE tnuec = ?",
                grupo.getNombre(),
                numeroEmpleadoResolvido
        );

        if (updated < 1) {
            throw new IllegalStateException("No fue posible asignar empleado al grupo");
        }

        GrupoEmpleado asignacion = new GrupoEmpleado();
        asignacion.setIdGrupo(idGrupo);
        asignacion.setNombreGrupo(grupo.getNombre());
        asignacion.setIdEmpleado(idEmpleado);
        asignacion.setNumeroEmpleado(numeroEmpleadoResolvido);
        asignacion.setActivo(true);
        asignacion.setFechaAsignacion(LocalDateTime.now());
        asignacion.setUsuarioAsigno(usuarioAsigno);

        persistirMetadataAsignacion(idGrupo, idEmpleado, numeroEmpleadoResolvido, usuarioAsigno);

        return asignacion;
    }

    // ------- REPORTE DE GRUPOS -------
    public List<GrupoEmpleado> reporteGrupo(Long idGrupo) {
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));

        Map<String, AsignacionMeta> metadata = cargarMetadataAsignacion(idGrupo);

        return pddespensaJdbc.query(
                "SELECT tnuec, tbist, tbife FROM tmemp WHERE tgrup = ? ORDER BY tnuec",
                (rs, rowNum) -> {
                    String numeroEmpleado = rs.getString("tnuec");

                    GrupoEmpleado ge = new GrupoEmpleado();
                    ge.setIdGrupo(idGrupo);
                    ge.setNombreGrupo(grupo.getNombre());
                    ge.setNumeroEmpleado(numeroEmpleado);
                    ge.setIdEmpleado(parseLong(numeroEmpleado));
                    ge.setActivo(!"X".equalsIgnoreCase(rs.getString("tbist")));

                    if (rs.getTimestamp("tbife") != null) {
                        ge.setFechaAsignacion(rs.getTimestamp("tbife").toLocalDateTime());
                    }

                    AsignacionMeta meta = metadata.get(numeroEmpleado);
                    if (meta != null) {
                        if (meta.fechaAsignacion() != null) {
                            ge.setFechaAsignacion(meta.fechaAsignacion());
                        }
                        ge.setUsuarioAsigno(meta.usuarioAsigno());
                    }
                    return ge;
                },
                grupo.getNombre()
        );
    }

    private Map<String, AsignacionMeta> cargarMetadataAsignacion(Long idGrupo) {
        Map<String, AsignacionMeta> result = new HashMap<>();
        try {
            dbdespensaJdbc.query(
                    "SELECT numero_empleado, fecha_asignacion, usuario_asigno " +
                            "FROM grupo_empleado_tec WHERE id_grupo = ? AND activo = TRUE " +
                            "ORDER BY fecha_asignacion DESC",
                    rs -> {
                        String numeroEmpleado = rs.getString("numero_empleado");
                        if (!result.containsKey(numeroEmpleado)) {
                            LocalDateTime fecha = rs.getTimestamp("fecha_asignacion") != null
                                    ? rs.getTimestamp("fecha_asignacion").toLocalDateTime()
                                    : null;
                            result.put(numeroEmpleado, new AsignacionMeta(fecha, rs.getString("usuario_asigno")));
                        }
                    },
                    idGrupo
            );
        } catch (DataAccessException ex) {
            log.warn("No se pudo consultar historial de asignacion; se usa solo tmemp. Causa: {}", ex.getMessage());
        }
        return result;
    }

    private record AsignacionMeta(LocalDateTime fechaAsignacion, String usuarioAsigno) {}

    private void persistirMetadataAsignacion(Long idGrupo, Long idEmpleado, String numeroEmpleado, String usuarioAsigno) {
        try {
            int updated = dbdespensaJdbc.update(
                    "UPDATE grupo_empleado_tec SET id_empleado = ?, activo = TRUE, fecha_asignacion = NOW(), usuario_asigno = ? " +
                            "WHERE id_grupo = ? AND numero_empleado = ?",
                    idEmpleado,
                    usuarioAsigno,
                    idGrupo,
                    numeroEmpleado
            );

            if (updated < 1) {
                dbdespensaJdbc.update(
                        "INSERT INTO grupo_empleado_tec (id_grupo, id_empleado, numero_empleado, activo, fecha_asignacion, usuario_asigno) " +
                                "VALUES (?, ?, ?, TRUE, NOW(), ?)",
                        idGrupo,
                        idEmpleado,
                        numeroEmpleado,
                        usuarioAsigno
                );
            }
        } catch (DataAccessException ex) {
            // No bloquea la asignacion  si no existe la tabla de historial en el ambiente.
            log.warn("No se pudo persistir metadata de asignacion en grupo_empleado_tec: {}", ex.getMessage());
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveGrupoId(GrupoDto dto) {
        String grupoId = dto.getGrupoid();
        if (grupoId == null || grupoId.isBlank()) {
            grupoId = dto.getNombre();
        }
        if (grupoId == null || grupoId.isBlank()) {
            throw new IllegalArgumentException("grupoid es obligatorio");
        }
        return grupoId.trim().toUpperCase();
    }

    public List<GrupoListadoDto> listarGrupos() {
        // Obtener contexto de sesiÃ³n
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();
        
        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de cliente/consignatario de la sesiÃ³n");
        }
        
        return dbdespensaJdbc.query(
            "SELECT dg.iddirecciones, dg.grupoid, dg.descripcion, dd.calle, dd.numero, dd.colonia, dd.codigopostal, " +
                "dd.delegacion, dd.estado, dd.nombre, dd.telefono, dd.nombre2, dd.telefono2, dd.horario, " +
                "dg.estatus, COALESCE(dg.fechamodificacion, dg.fechacreacion) AS fecha, dd.observacion " +
                "FROM direccionesgrupo dg " +
                "LEFT JOIN detalledirecciones dd ON dd.iddirecciones = dg.iddirecciones " +
                "WHERE dg.clienteid = ? AND dg.consignatarioid = ? " +
                "ORDER BY dg.iddirecciones DESC",
            (rs, rowNum) -> {
                GrupoListadoDto dto = new GrupoListadoDto();
                dto.setIddirecciones(rs.getLong("iddirecciones"));
                dto.setGrupoid(rs.getString("grupoid"));
                dto.setDescripcion(rs.getString("descripcion"));
                dto.setCalle(rs.getString("calle"));
                dto.setNumero(rs.getString("numero"));
                dto.setColonia(rs.getString("colonia"));
                dto.setCodigopostal(rs.getString("codigopostal"));
                dto.setDelegacion(rs.getString("delegacion"));
                dto.setEstado(rs.getString("estado"));
                dto.setNombre(rs.getString("nombre"));
                dto.setTelefono(rs.getString("telefono"));
                dto.setNombre2(rs.getString("nombre2"));
                dto.setTelefono2(rs.getString("telefono2"));
                dto.setHorario(rs.getString("horario"));
                dto.setEstatus(rs.getObject("estatus", Boolean.class));
                dto.setFecha(rs.getTimestamp("fecha") != null ? rs.getTimestamp("fecha").toLocalDateTime() : null);
                dto.setObservacion(rs.getString("observacion"));
                return dto;
            },
            clienteId,
            consignatarioId
        );
    }

    public Grupo obtenerGrupo(Long idGrupo) {
        // Obtener contexto de sesiÃ³n
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();
        
        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de cliente/consignatario de la sesiÃ³n");
        }
        
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));
        
        // Validar que el grupo pertenece al cliente/consignatario autenticado
        if (!clienteId.equals(grupo.getClienteId()) || !consignatarioId.equals(grupo.getConsignatarioId())) {
            throw new IllegalArgumentException("No tiene permisos para acceder a este grupo");
        }
        
        return grupo;
    }
}

