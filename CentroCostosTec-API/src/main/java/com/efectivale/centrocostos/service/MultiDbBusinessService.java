package com.efectivale.centrocostos.service;

import com.efectivale.centrocostos.dto.EmpleadoDto;
import com.efectivale.centrocostos.dto.GrupoDto;


import com.efectivale.centrocostos.dto.SolicitudDto;
import com.efectivale.centrocostos.dto.CredencialOperacionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class MultiDbBusinessService {

    private static final Logger log = LoggerFactory.getLogger(MultiDbBusinessService.class);

    /** BD primaria (dbdespensa) â€” contiene: corporativos, centrocostos, corpusuarios, etc. */
    private final JdbcTemplate dbdespensaJdbc;
    private final JdbcTemplate pddespensaJdbc;
    private final JdbcTemplate megadbpedidoJdbc;
    private final JdbcTemplate dbemisJdbc;

    @Value("${app.multidb.strict:false}")
    private boolean strict;

    public MultiDbBusinessService(
            @Qualifier("dbdespensaJdbc")   JdbcTemplate dbdespensaJdbc,
            @Qualifier("pddespensaJdbc")   JdbcTemplate pddespensaJdbc,
            @Qualifier("megadbpedidoJdbc") JdbcTemplate megadbpedidoJdbc,
            @Qualifier("dbemisJdbc")       JdbcTemplate dbemisJdbc) {
        this.dbdespensaJdbc   = dbdespensaJdbc;
        this.pddespensaJdbc   = pddespensaJdbc;
        this.megadbpedidoJdbc = megadbpedidoJdbc;
        this.dbemisJdbc       = dbemisJdbc;
    }

    public void validarContextoSolicitud(SolicitudDto dto, String tipoSolicitud) {
        validarConexionMegadbpedido("Solicitud-" + tipoSolicitud);
        validarConexionPddespensa("Solicitud-" + tipoSolicitud);
        validarContextoInformix(dto.getClienteId(), dto.getConsignatarioId(), "Solicitud-" + tipoSolicitud);
    }

    public void validarContextoCredencial(CredencialOperacionDto dto, String operacion) {
        validarConexionPddespensa("Credencial-" + operacion);
        validarContextoInformix(dto.getClienteId(), dto.getConsignatarioId(), "Credencial-" + operacion);
    }

    public void validarContextoGrupo(GrupoDto dto, String operacion) {
        validarConexionPddespensa("Grupos-" + operacion);
        if (dto != null) {
            validarContextoInformix(dto.getClienteId(), dto.getConsignatarioId(), "Grupos-" + operacion);
        }
    }

    public void validarContextoGrupo(Long clienteId, Long consignatarioId, String operacion) {
        validarConexionPddespensa("Grupos-" + operacion);
        validarContextoInformix(clienteId, consignatarioId, "Grupos-" + operacion);
    }

    public void validarContextoEmpleado(EmpleadoDto dto, String operacion) {
        validarConexionPddespensa("Empleados-" + operacion);
        validarContextoInformix(dto.getClienteId(), dto.getConsignatarioId(), "Empleados-" + operacion);
        if (dto.getNumeroEmpleado() != null && !dto.getNumeroEmpleado().isBlank()) {
            validarEmpleadoInformix(dto.getNumeroEmpleado());
        }
    }

    /**
     * ValidaciÃ³n de contexto login:
     *  - Verifica conectividad con dbdespensa (contiene corpusuarios, corporativos, centrocostos)
     *  Nota: corpusuarios estÃ¡ en dbdespensa (PostgreSQL), NO en Informix.
     */
    public void validarContextoLogin(String username) {
        validarConexionDbdespensa("Auth-login");
    }

    // -----------------------------------------------------------------------
    // Conexiones
    // -----------------------------------------------------------------------

    private void validarConexionDbdespensa(String contexto) {
        ejecutar("dbdespensa", contexto, () -> dbdespensaJdbc.queryForObject("SELECT CURRENT_TIMESTAMP", Object.class));
    }

    private void validarConexionPddespensa(String contexto) {
        ejecutar("pddespensa", contexto, () -> pddespensaJdbc.queryForObject("SELECT CURRENT_TIMESTAMP", Object.class));
    }

    private void validarConexionMegadbpedido(String contexto) {
        ejecutar("megadbpedido", contexto, () -> megadbpedidoJdbc.queryForObject("SELECT CURRENT_TIMESTAMP", Object.class));
    }

    /**
     * Valida que cliente+consignatario existan en la tabla tcope de Informix (dbemis).
     * Equivalente al join que hace el  con dbemis.
     */
    private void validarContextoInformix(Long clienteId, Long consignatarioId, String contexto) {
        if (clienteId == null || consignatarioId == null) {
            return;
        }
        ejecutar("dbemis", contexto, () -> dbemisJdbc.queryForObject(
                "SELECT FIRST 1 tnucl FROM tcope WHERE tnucl = ? AND tnuco = ?",
                Long.class,
                clienteId,
                consignatarioId
        ));
    }

    /**
     * Valida que el nÃºmero de empleado exista en tmemp de Informix (dbemis).
     */
    private void validarEmpleadoInformix(String numeroEmpleado) {
        ejecutar("dbemis", "Empleados-existe", () -> dbemisJdbc.queryForObject(
                "SELECT FIRST 1 tnuec FROM tmemp WHERE tnuec = ?",
                String.class,
                numeroEmpleado
        ));
    }

    private void ejecutar(String base, String contexto, Supplier<Object> accion) {
        try {
            accion.get();
            log.info("Validacion multi-base OK [{}] en {}", contexto, base);
        } catch (Exception ex) {
            String msg = "Validacion multi-base fallo [" + contexto + "] en " + base + ": " + ex.getMessage();
            if (strict) {
                throw new IllegalStateException(msg, ex);
            }
            log.warn(msg);
        }
    }
}


