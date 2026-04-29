package com.efectivale.centrocostos.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GrupoListadoDto {
    private Long iddirecciones;
    private String grupoid;
    private String descripcion;
    private String calle;
    private String numero;
    private String colonia;
    private String codigopostal;
    private String delegacion;
    private String estado;
    private String nombre;
    private String telefono;
    private String nombre2;
    private String telefono2;
    private String horario;
    private Boolean estatus;
    private LocalDateTime fecha;
    private String observacion;
}