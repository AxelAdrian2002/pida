package com.efectivale.centrocostos.service;

import com.efectivale.centrocostos.dto.EmpleadoDto;
import com.efectivale.centrocostos.entity.Empleado;
import com.efectivale.centrocostos.repository.EmpleadoRepository;
import com.efectivale.centrocostos.security.ContextProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private static final Logger log = LoggerFactory.getLogger(EmpleadoService.class);
    private final EmpleadoRepository empleadoRepository;
    private final @Qualifier("dbdespensaJdbc") JdbcTemplate dbdespensaJdbc;
    private final @Qualifier("pddespensaJdbc") JdbcTemplate pddespensaJdbc;
    private final MultiDbBusinessService multiDbBusinessService;
    private final ContextProvider contextProvider;

    public Page<Empleado> consultarEmpleados(String nombre, String departamento, Pageable pageable) {
        // Obtener contexto de sesión
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();
        
        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de cliente/consignatario de la sesión");
        }

        String nombreFiltro = nombre != null ? nombre.trim().toLowerCase() : "";
        String numeroEmpleado = departamento != null ? departamento.trim() : "";

        StringBuilder sql = new StringBuilder(
                "SELECT tidem, tnuec, tnoem, tappa, tapma, tmail, ttele, tnucl, tnuco, tbist, tbife " +
                        "FROM tmemp WHERE tnucl = ? AND tnuco = ? AND (tbist IS NULL OR tbist <> 'X')"
        );

        List<Object> params = new ArrayList<>();
        params.add(clienteId);
        params.add(consignatarioId);

        if (!nombreFiltro.isBlank()) {
            sql.append(" AND LOWER(tnoem) LIKE ?");
            params.add("%" + nombreFiltro + "%");
        }
        if (!numeroEmpleado.isBlank()) {
            sql.append(" AND tnuec = ?");
            params.add(numeroEmpleado);
        }

        sql.append(" ORDER BY tnuec");

        List<Empleado> todos = pddespensaJdbc.query(
                sql.toString(),
                (rs, rowNum) -> mapEmpleado(rs),
                params.toArray()
        );

        int total = todos.size();
        int from = (int) pageable.getOffset();
        int to = Math.min(from + pageable.getPageSize(), total);

        if (from >= total) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        return new PageImpl<>(todos.subList(from, to), pageable, total);
    }

    public Empleado obtenerPorNumero(String numeroEmpleado) {
        // Obtener contexto de sesión
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();
        
        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de cliente/consignatario de la sesión");
        }

        try {
            return pddespensaJdbc.queryForObject(
                    "SELECT tidem, tnuec, tnoem, tappa, tapma, tmail, ttele, tnucl, tnuco, tbist, tbife " +
                            "FROM tmemp WHERE tnuec = ? AND tnucl = ? AND tnuco = ?",
                    (rs, rowNum) -> mapEmpleado(rs),
                    numeroEmpleado,
                    clienteId,
                    consignatarioId
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new IllegalArgumentException("Empleado no encontrado: " + numeroEmpleado);
        }
    }

    @Transactional
    public Empleado actualizarEmpleado(String numeroEmpleado, EmpleadoDto dto) {
        log.info("Actualizando datos del empleado {}", numeroEmpleado);
        multiDbBusinessService.validarContextoEmpleado(dto, "ACTUALIZAR");
        Empleado empleado = obtenerPorNumero(numeroEmpleado);

        int actualizados = pddespensaJdbc.update(
                "UPDATE tmemp SET tmail = COALESCE(?, tmail), ttele = COALESCE(?, ttele), " +
                        "tappa = COALESCE(?, tappa), tapma = COALESCE(?, tapma), tnoem = COALESCE(?, tnoem) " +
                        "WHERE tnuec = ?",
                dto.getEmail(),
                dto.getTelefono(),
                dto.getApellidoPaterno(),
                dto.getApellidoMaterno(),
                dto.getNombre(),
                numeroEmpleado
        );

        if (actualizados < 1) {
            throw new IllegalStateException("No fue posible actualizar el empleado " + numeroEmpleado);
        }

        // Actualizar entidad
        if (dto.getEmail() != null) empleado.setEmail(dto.getEmail());
        if (dto.getTelefono() != null) empleado.setTelefono(dto.getTelefono());
        if (dto.getApellidoPaterno() != null) empleado.setApellidoPaterno(dto.getApellidoPaterno());
        if (dto.getApellidoMaterno() != null) empleado.setApellidoMaterno(dto.getApellidoMaterno());
        if (dto.getNombre() != null) empleado.setNombre(dto.getNombre());
        empleado.setUsuarioModificacion(dto.getUsuarioModificacion());

        return empleado;
    }

    private Empleado mapEmpleado(java.sql.ResultSet rs) throws java.sql.SQLException {
        Empleado empleado = new Empleado();
        empleado.setIdEmpleado(rs.getLong("tidem"));
        empleado.setNumeroEmpleado(rs.getString("tnuec"));
        empleado.setNombre(rs.getString("tnoem"));
        empleado.setApellidoPaterno(rs.getString("tappa"));
        empleado.setApellidoMaterno(rs.getString("tapma"));
        empleado.setEmail(rs.getString("tmail"));
        empleado.setTelefono(rs.getString("ttele"));
        empleado.setClienteId(rs.getLong("tnucl"));
        empleado.setConsignatarioId(rs.getLong("tnuco"));
        empleado.setEstatusId(rs.getString("tbist"));
        if (rs.getTimestamp("tbife") != null) {
            empleado.setFechaAlta(rs.getTimestamp("tbife").toLocalDateTime());
        }
        return empleado;
    }

    @Transactional
    public Empleado altaEmpleado(EmpleadoDto dto) {
        multiDbBusinessService.validarContextoEmpleado(dto, "ALTA");
        if (empleadoRepository.existsByNumeroEmpleado(dto.getNumeroEmpleado())) {
            throw new IllegalArgumentException("Ya existe un empleado con el numero: " + dto.getNumeroEmpleado());
        }
        Empleado empleado = new Empleado();
        empleado.setNumeroEmpleado(dto.getNumeroEmpleado());
        empleado.setNombre(dto.getNombre());
        empleado.setApellidoPaterno(dto.getApellidoPaterno());
        empleado.setApellidoMaterno(dto.getApellidoMaterno());
        empleado.setEmail(dto.getEmail());
        empleado.setTelefono(dto.getTelefono());
        return empleadoRepository.save(empleado);
    }
}
