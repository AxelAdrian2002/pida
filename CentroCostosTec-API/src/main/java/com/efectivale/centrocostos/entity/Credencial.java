package com.efectivale.centrocostos.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "credencial_interna")
public class Credencial {

    @Id
    @Column(name = "tarjetaid")
    private Long idCredencial;

    @Transient
    private String numeroCredencial;

    @Transient
    private Long idEmpleado;

    @Column(name = "empleadoid", nullable = false, length = 20)
    private String numeroEmpleado;

    @Column(name = "empleadonombre", nullable = false, length = 200)
    private String nombreEmpleado;

    @Column(name = "estado", insertable = false, updatable = false)
    private String estado;

    @Transient
    private Long idGrupo;

    @Column(name = "cuentaid", nullable = false, length = 30)
    private String cuentaId;

    @Column(name = "parametrosid", nullable = false)
    private Long parametrosId;

    @Column(name = "tarjetatipo", nullable = false, length = 1)
    private String tipoCredencial;

    @Column(name = "tarjetafechacreacion", nullable = false)
    private LocalDateTime fechaEmision;

    @Transient
    private LocalDateTime fechaActivacion;

    @Transient
    private LocalDateTime fechaCancelacion;

    @Transient
    private String motivoCancelacion;

    @Transient
    private String usuarioOperacion;

    @Column(name = "tarjetafechamodificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    @Column(name = "tarjetacancelada", nullable = false)
    private Boolean credencialCancelada;

    @Column(name = "clienteid", nullable = false)
    private Long clienteId;

    @Column(name = "consignatarioid", nullable = false)
    private Long consignatarioId;

    public String getNumeroCredencial() {
        return idCredencial != null ? String.valueOf(idCredencial) : null;
    }

    public void setNumeroCredencial(String numeroCredencial) {
        this.numeroCredencial = numeroCredencial;
        if (numeroCredencial != null && !numeroCredencial.isBlank()) {
            try {
                this.idCredencial = Long.valueOf(numeroCredencial.trim());
            } catch (NumberFormatException ignored) {
                this.idCredencial = null;
            }
        }
    }

    public Long getIdEmpleado() {
        if (numeroEmpleado == null || numeroEmpleado.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(numeroEmpleado.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
        if (idEmpleado != null) {
            this.numeroEmpleado = String.valueOf(idEmpleado);
        }
    }

    public Long getIdCredencial() {
        return idCredencial;
    }
}
