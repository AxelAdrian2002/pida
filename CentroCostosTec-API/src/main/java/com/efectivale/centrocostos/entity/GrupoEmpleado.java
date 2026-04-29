package com.efectivale.centrocostos.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GrupoEmpleado {

    private Long idAsignacion;

    private Long idGrupo;

    private String nombreGrupo;

    private Long idEmpleado;

    private String numeroEmpleado;

    private Boolean activo = true;

    private LocalDateTime fechaAsignacion;

    private String usuarioAsigno;

    public String getNumeroEmpleado() {
        return numeroEmpleado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public String getUsuarioAsigno() {
        return usuarioAsigno;
    }
}
