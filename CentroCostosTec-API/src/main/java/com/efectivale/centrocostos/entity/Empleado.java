package com.efectivale.centrocostos.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tmemp")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tidem")
    private Long idEmpleado;

    @Column(name = "tnuec", nullable = false, unique = true, length = 20)
    private String numeroEmpleado;

    @Column(name = "tnoem", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tappa", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "tapma", length = 100)
    private String apellidoMaterno;

    @Column(name = "tmail", length = 100)
    private String email;

    @Column(name = "ttele", length = 20)
    private String telefono;

    @Column(name = "tnucl")
    private Long clienteId;

    @Column(name = "tnuco")
    private Long consignatarioId;

    @Column(name = "tbist", nullable = false, length = 1)
    private String estatusId;

    @Column(name = "tbife", nullable = false)
    private LocalDateTime fechaAlta;

    @Transient
    private LocalDateTime fechaModificacion;

    @Transient
    private String usuarioModificacion;

    @Transient
    private Boolean activo;

    @PrePersist
    protected void onCreate() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
        if (estatusId == null || estatusId.isBlank()) {
            estatusId = "A";
        }
    }

    public Boolean getActivo() {
        if (activo != null) {
            return activo;
        }
        return estatusId == null || !"X".equalsIgnoreCase(estatusId);
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
        if (activo == null) {
            return;
        }
        this.estatusId = activo ? "A" : "X";
    }
}
