package com.efectivale.centrocostos.service;

import com.efectivale.centrocostos.dto.CredencialOperacionDto;
import com.efectivale.centrocostos.entity.Credencial;
import com.efectivale.centrocostos.entity.CredencialBitacora;
import com.efectivale.centrocostos.repository.CredencialBitacoraRepository;
import com.efectivale.centrocostos.repository.CredencialRepository;
import com.efectivale.centrocostos.security.ContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CredencialService {

    private static final Logger log = LoggerFactory.getLogger(CredencialService.class);
    private final CredencialRepository credencialRepository;
    private final CredencialBitacoraRepository bitacoraRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ContextProvider contextProvider;
    private final TenantAuditService tenantAuditService;

    public CredencialService(
            CredencialRepository credencialRepository,
            CredencialBitacoraRepository bitacoraRepository,
            @Qualifier("pddespensaJdbc") JdbcTemplate jdbcTemplate,
            ContextProvider contextProvider,
            TenantAuditService tenantAuditService) {
        this.credencialRepository = credencialRepository;
        this.bitacoraRepository = bitacoraRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.contextProvider = contextProvider;
        this.tenantAuditService = tenantAuditService;
    }

    // ------- CONSULTA CON FILTROS -------
    public Page<Credencial> consultarCredenciales(String estado, Long idGrupo,
                                                   String numeroEmpleado, Pageable pageable) {
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();

        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de la sesión");
        }

        return credencialRepository.buscarConFiltros(estado, numeroEmpleado, clienteId, consignatarioId, pageable);
    }

    public Credencial obtenerPorNumero(String numeroCredencial) {
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();

        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de la sesión");
        }

        return credencialRepository.findByNumeroCredencial(numeroCredencial, clienteId, consignatarioId)
                .orElseThrow(() -> new IllegalArgumentException("Credencial no encontrada: " + numeroCredencial));
    }

    public List<CredencialBitacora> obtenerBitacora(Long idCredencial) {
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();

        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de la sesión");
        }

        return bitacoraRepository.findByIdCredencialAndTenantOrderByFechaOperacionDesc(
            idCredencial,
            clienteId,
            consignatarioId
        );
    }

    // ------- ACTIVAR CREDENCIAL -------
    @Transactional
    public Credencial activarCredencial(CredencialOperacionDto dto) {
        completarContexto(dto);
        log.info("Activando credencial {}", dto.getNumeroCredencial());
        Credencial credencial = obtenerPorNumero(dto.getNumeroCredencial());

        if ("ACTIVA".equals(credencial.getEstado())) {
            throw new IllegalArgumentException("La credencial ya se encuentra activa");
        }
        if ("CANCELADA".equals(credencial.getEstado())) {
            throw new IllegalArgumentException("No se puede activar una credencial cancelada");
        }

        jdbcTemplate.update(
                "UPDATE estado_credencial SET parametrosactiva = TRUE WHERE parametrosid = ?",
                credencial.getParametrosId()
        );
        registrarBitacora(credencial, dto, "INACTIVA", "ACTIVA");
        tenantAuditService.logFromContext(
            "CREDENCIALES",
            "ACTIVAR_CREDENCIAL",
            "Credencial activada id=" + credencial.getIdCredencial(),
            contextProvider
        );
        return obtenerPorNumero(dto.getNumeroCredencial());
    }

    @Transactional
    public Credencial inactivarCredencial(CredencialOperacionDto dto) {
        completarContexto(dto);
        log.info("Inactivando credencial {}", dto.getNumeroCredencial());
        Credencial credencial = obtenerPorNumero(dto.getNumeroCredencial());

        if ("INACTIVA".equals(credencial.getEstado())) {
            throw new IllegalArgumentException("La credencial ya se encuentra inactiva");
        }
        if ("CANCELADA".equals(credencial.getEstado())) {
            throw new IllegalArgumentException("No se puede inactivar una credencial cancelada");
        }

        jdbcTemplate.update(
                "UPDATE estado_credencial SET parametrosactiva = FALSE WHERE parametrosid = ?",
                credencial.getParametrosId()
        );
        registrarBitacora(credencial, dto, "ACTIVA", "INACTIVA");
        tenantAuditService.logFromContext(
            "CREDENCIALES",
            "INACTIVAR_CREDENCIAL",
            "Credencial inactivada id=" + credencial.getIdCredencial(),
            contextProvider
        );
        return obtenerPorNumero(dto.getNumeroCredencial());
    }

    // ------- CANCELAR CREDENCIAL -------
    @Transactional
    public Credencial cancelarCredencial(CredencialOperacionDto dto) {
        completarContexto(dto);
        log.info("Cancelando credencial {}", dto.getNumeroCredencial());
        Credencial credencial = obtenerPorNumero(dto.getNumeroCredencial());

        if ("CANCELADA".equals(credencial.getEstado())) {
            throw new IllegalArgumentException("La credencial ya se encuentra cancelada");
        }

        jdbcTemplate.update(
                "UPDATE credencial_interna SET tarjetacancelada = TRUE, tarjetafechamodificacion = NOW() WHERE tarjetaid = ?",
                credencial.getIdCredencial()
        );
        registrarBitacora(credencial, dto, credencial.getEstado(), "CANCELADA");

        tenantAuditService.logFromContext(
            "CREDENCIALES",
            "CANCELAR_CREDENCIAL",
            "Credencial cancelada id=" + credencial.getIdCredencial(),
            contextProvider
        );

        credencial.setEstado("CANCELADA");
        credencial.setMotivoCancelacion(dto.getObservacion());
        credencial.setUsuarioOperacion(dto.getUsuarioOperacion());
        return credencial;
    }

    private void registrarBitacora(Credencial credencial, CredencialOperacionDto dto,
                                    String estadoAnterior, String estadoNuevo) {
        CredencialBitacora bitacora = new CredencialBitacora();
        bitacora.setIdCredencial(credencial.getIdCredencial());
        bitacora.setEstadoAnterior(estadoAnterior);
        bitacora.setEstadoNuevo(estadoNuevo);
        bitacora.setIdUsuario(dto.getIdUsuario());
        bitacora.setUsuarioOperacion(dto.getUsuarioOperacion());
        bitacora.setObservacion(dto.getObservacion());
        bitacoraRepository.save(bitacora);
    }

    private void completarContexto(CredencialOperacionDto dto) {
        if (dto.getClienteId() == null) {
            dto.setClienteId(contextProvider.getClienteId());
        }
        if (dto.getConsignatarioId() == null) {
            dto.setConsignatarioId(contextProvider.getConsignatarioId());
        }
        if (dto.getIdUsuario() == null) {
            dto.setIdUsuario(contextProvider.getIdUsuario());
        }
        if (dto.getBitacoraId() == null) {
            dto.setBitacoraId(-1);
        }
    }
}
