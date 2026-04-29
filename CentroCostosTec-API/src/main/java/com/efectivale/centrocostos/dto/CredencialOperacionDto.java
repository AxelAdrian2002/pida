package com.efectivale.centrocostos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CredencialOperacionDto {

    @NotBlank(message = "El numero de credencial es obligatorio")
    private String numeroCredencial;

    private String observacion;
    private Long idUsuario;
    private Integer bitacoraId;
    private String usuarioOperacion;

    private Long clienteId;
    private Long consignatarioId;

    public String getNumeroCredencial() {
        return numeroCredencial;
    }

    public void setNumeroCredencial(String numeroCredencial) {
        this.numeroCredencial = numeroCredencial;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getBitacoraId() {
        return bitacoraId;
    }

    public void setBitacoraId(Integer bitacoraId) {
        this.bitacoraId = bitacoraId;
    }

    public String getUsuarioOperacion() {
        return usuarioOperacion;
    }

    public void setUsuarioOperacion(String usuarioOperacion) {
        this.usuarioOperacion = usuarioOperacion;
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
}
