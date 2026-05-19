package com.efectivale.centrocostos.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efectivale.centrocostos.security.ContextProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SolicitudAuditoriaService {
    
    private static final Logger log = LoggerFactory.getLogger(SolicitudAuditoriaService.class);
    
    private final @Qualifier("megadbpedidoJdbc") JdbcTemplate megadbpedidoJdbc;
    private final ContextProvider contextProvider;
    private final ObjectMapper objectMapper;

    /**
     * Registra un cambio de solicitud en la tabla de auditoría
     */
    @Transactional
    public void registrarCambio(
            Long solicitudId,
            Long clienteId,
            Long consignatarioId,
            Long usuarioId,
            String usuarioNombre,
            String accion,
            String tipoSolicitud,
            String estadoAnterior,
            String estadoNuevo,
            String motivoCambio,
            Map<String, Object> datosAnteriores,
            Map<String, Object> datosNuevos) {
        
        try {
            String datosAnterioresJson = datosAnteriores != null ? objectMapper.writeValueAsString(datosAnteriores) : "{}";
            String datosNuevosJson = datosNuevos != null ? objectMapper.writeValueAsString(datosNuevos) : "{}";
            
            LocalDateTime now = LocalDateTime.now();
            
            megadbpedidoJdbc.update(
                "INSERT INTO solicitud_auditoria " +
                "(solicitud_id, cliente_id, consignatario_id, usuario_id, usuario_nombre, accion, tipo_solicitud, " +
                "estado_anterior, estado_nuevo, motivo_cambio, datos_anteriores, datos_nuevos, fecha_cambio, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                solicitudId,
                clienteId,
                consignatarioId,
                usuarioId,
                usuarioNombre,
                accion,
                tipoSolicitud,
                estadoAnterior,
                estadoNuevo,
                motivoCambio,
                datosAnterioresJson,
                datosNuevosJson,
                Timestamp.valueOf(now),
                true
            );
            
            log.info("Auditoría registrada: Solicitud {} - Acción {} - Usuario {}", 
                solicitudId, accion, usuarioNombre);
        } catch (Exception ex) {
            log.error("Error registrando auditoría para solicitud {}: {}", solicitudId, ex.getMessage(), ex);
            // No lanzar excepción para que el cambio se registre aunque falle la auditoría
        }
    }

    /**
     * Registra la creación de una solicitud
     */
    @Transactional
    public void registrarCreacion(
            Long solicitudId,
            Long clienteId,
            Long consignatarioId,
            Long usuarioId,
            String usuarioNombre,
            String tipoSolicitud,
            Map<String, Object> datosCreacion) {
        
        registrarCambio(
            solicitudId,
            clienteId,
            consignatarioId,
            usuarioId,
            usuarioNombre,
            "CREAR",
            tipoSolicitud,
            null,
            "PENDIENTE",
            "Solicitud creada",
            new HashMap<>(),
            datosCreacion
        );
    }

    /**
     * Registra la autorización de una solicitud
     */
    @Transactional
    public void registrarAutorizacion(
            Long solicitudId,
            Long clienteId,
            Long consignatarioId,
            Long usuarioId,
            String usuarioNombre,
            String tipoSolicitud,
            String observaciones) {
        
        registrarCambio(
            solicitudId,
            clienteId,
            consignatarioId,
            usuarioId,
            usuarioNombre,
            "AUTORIZAR",
            tipoSolicitud,
            "PENDIENTE",
            "AUTORIZADO",
            observaciones != null ? observaciones : "Solicitud autorizada",
            new HashMap<>(),
            new HashMap<>()
        );
    }

    /**
     * Registra el rechazo de una solicitud
     */
    @Transactional
    public void registrarRechazo(
            Long solicitudId,
            Long clienteId,
            Long consignatarioId,
            Long usuarioId,
            String usuarioNombre,
            String tipoSolicitud,
            String motivo) {
        
        registrarCambio(
            solicitudId,
            clienteId,
            consignatarioId,
            usuarioId,
            usuarioNombre,
            "RECHAZAR",
            tipoSolicitud,
            "PENDIENTE",
            "RECHAZADO",
            motivo,
            new HashMap<>(),
            new HashMap<>()
        );
    }

    /**
     * Registra la cancelación de una solicitud
     */
    @Transactional
    public void registrarCancelacion(
            Long solicitudId,
            Long clienteId,
            Long consignatarioId,
            Long usuarioId,
            String usuarioNombre,
            String tipoSolicitud,
            String motivo) {
        
        registrarCambio(
            solicitudId,
            clienteId,
            consignatarioId,
            usuarioId,
            usuarioNombre,
            "CANCELAR",
            tipoSolicitud,
            null, // Estado anterior puede ser PENDIENTE o AUTORIZADO
            "CANCELADO",
            motivo,
            new HashMap<>(),
            new HashMap<>()
        );
    }

    /**
     * Obtiene el historial de auditoría de una solicitud
     */
    public java.util.List<Map<String, Object>> obtenerHistorialSolicitud(Long solicitudId) {
        return megadbpedidoJdbc.queryForList(
            "SELECT * FROM solicitud_auditoria WHERE solicitud_id = ? AND activo = TRUE ORDER BY fecha_cambio DESC",
            solicitudId
        );
    }

    /**
     * Obtiene auditoría por cliente y rango de fechas (para reportes)
     */
    public java.util.List<Map<String, Object>> obtenerAuditoriaPorCliente(
            Long clienteId,
            Long consignatarioId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
        
        return megadbpedidoJdbc.queryForList(
            "SELECT * FROM solicitud_auditoria " +
            "WHERE cliente_id = ? AND consignatario_id = ? " +
            "AND fecha_cambio >= ? AND fecha_cambio <= ? " +
            "AND activo = TRUE " +
            "ORDER BY fecha_cambio DESC",
            clienteId,
            consignatarioId,
            Timestamp.valueOf(fechaInicio),
            Timestamp.valueOf(fechaFin)
        );
    }
}
