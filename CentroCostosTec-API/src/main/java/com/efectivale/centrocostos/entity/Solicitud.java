package com.efectivale.centrocostos.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Solicitud {

    private Long idSolicitud;
    private String tipoSolicitud;
    private String estado;
    private BigDecimal montoTotal;
    private Long idUsuario;
    private String descripcion;
    private String referencia;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAutorizacion;
    private String usuarioAutorizo;
    private String observaciones;
    private List<SolicitudDetalle> detalles;
    private String estadoId;
    private Long clienteId;
    private Long consignatarioId;
    private Long confirmacionId;
    private Long movimientoId;
    private Boolean migrado;
    private Boolean activo;

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public void setFechaAutorizacion(LocalDateTime fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    public String getUsuarioAutorizo() {
        return usuarioAutorizo;
    }

    public void setUsuarioAutorizo(String usuarioAutorizo) {
        this.usuarioAutorizo = usuarioAutorizo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<SolicitudDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<SolicitudDetalle> detalles) {
        this.detalles = detalles;
    }

    public String getEstadoId() {
        return estadoId;
    }

    public void setEstadoId(String estadoId) {
        this.estadoId = estadoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getConsignatarioId() {
        return consignatarioId;
    }

    public void setConsignatarioId(Long consignatarioId) {
        this.consignatarioId = consignatarioId;
    }

    public Long getConfirmacionId() {
        return confirmacionId;
    }

    public void setConfirmacionId(Long confirmacionId) {
        this.confirmacionId = confirmacionId;
    }

    public Long getMovimientoId() {
        return movimientoId;
    }

    public void setMovimientoId(Long movimientoId) {
        this.movimientoId = movimientoId;
    }

    public Boolean getMigrado() {
        return migrado;
    }

    public void setMigrado(Boolean migrado) {
        this.migrado = migrado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getEstado() {
        return switch (estadoId != null ? estadoId : "") {
            case "NVO", "FACA", "FACR", "PCRE" -> "PENDIENTE";
            case "PLIB" -> "AUTORIZADO";
            case "RECH" -> "RECHAZADO";
            case "CANP", "CANC" -> "CANCELADO";
            default -> estado != null ? estado : "PENDIENTE";
        };
    }

    public void setEstado(String estado) {
        this.estado = estado;
        this.estadoId = switch (estado != null ? estado.toUpperCase() : "") {
            case "AUTORIZADO" -> "PLIB";
            case "RECHAZADO" -> "RECH";
            case "CANCELADO" -> "CANP";
            default -> "NVO";
        };
    }
}
