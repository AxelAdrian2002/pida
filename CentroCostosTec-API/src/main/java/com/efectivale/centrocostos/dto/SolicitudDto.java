package com.efectivale.centrocostos.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SolicitudDto {

    @NotBlank(message = "El tipo de solicitud es obligatorio")
    private String tipoSolicitud;

    private String descripcion;
    private String referencia;
    private BigDecimal montoTotal;
    private BigDecimal precioBase;

    @NotNull(message = "El usuario es obligatorio")
    private Long idUsuario;

    private Long clienteId;
    private Long consignatarioId;
    private Long prefacturaId;

    private List<SolicitudDetalleDto> detalles;

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
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

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
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

    public Long getPrefacturaId() {
        return prefacturaId;
    }

    public void setPrefacturaId(Long prefacturaId) {
        this.prefacturaId = prefacturaId;
    }

    public List<SolicitudDetalleDto> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<SolicitudDetalleDto> detalles) {
        this.detalles = detalles;
    }

    @Data
    public static class SolicitudDetalleDto {
        private Long idEmpleado;
        private String numeroEmpleado;
        private String nombreEmpleado;
        private BigDecimal monto;
        private String descripcion;
        private String numeroCredencial;

        public Long getIdEmpleado() {
            return idEmpleado;
        }

        public void setIdEmpleado(Long idEmpleado) {
            this.idEmpleado = idEmpleado;
        }

        public String getNumeroEmpleado() {
            return numeroEmpleado;
        }

        public void setNumeroEmpleado(String numeroEmpleado) {
            this.numeroEmpleado = numeroEmpleado;
        }

        public String getNombreEmpleado() {
            return nombreEmpleado;
        }

        public void setNombreEmpleado(String nombreEmpleado) {
            this.nombreEmpleado = nombreEmpleado;
        }

        public BigDecimal getMonto() {
            return monto;
        }

        public void setMonto(BigDecimal monto) {
            this.monto = monto;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getNumeroCredencial() {
            return numeroCredencial;
        }

        public void setNumeroCredencial(String numeroCredencial) {
            this.numeroCredencial = numeroCredencial;
        }
    }
}
