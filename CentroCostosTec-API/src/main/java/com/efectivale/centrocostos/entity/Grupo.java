package com.efectivale.centrocostos.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "direccionesgrupo")
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddirecciones")
    private Long idGrupo;

    @Column(name = "grupoid", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "estatus", nullable = false)
    private Boolean activo = true;

    @Column(name = "clienteid")
    private Long clienteId;

    @Column(name = "consignatarioid")
    private Long consignatarioId;

    @Column(name = "fechacreacion", nullable = false)
    private LocalDateTime fechaAlta;

    @Transient
    private String usuarioAlta;

    @Transient
    private List<GrupoEmpleado> empleados;

    @PrePersist
    protected void onCreate() {
        fechaAlta = LocalDateTime.now();
    }
}
