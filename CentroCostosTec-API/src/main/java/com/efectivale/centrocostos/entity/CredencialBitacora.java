package com.efectivale.centrocostos.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bitacora_credencial")
public class CredencialBitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bitacoraid")
    private Long idBitacora;

    @Column(name = "tarjetaid", nullable = false)
    private Long idCredencial;

    @Transient
    private String numeroCredencial;

    @Column(name = "estatusanterior", length = 20)
    private String estadoAnterior;

    @Column(name = "estatusid", nullable = false, length = 20)
    private String estadoNuevo;

    @Column(name = "usuarioid")
    private Long idUsuario;

    @Column(name = "usuario_operacion", length = 50)
    private String usuarioOperacion;

    @Column(name = "observacion", length = 300)
    private String observacion;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDateTime fechaOperacion;

    @PrePersist
    protected void onCreate() {
        fechaOperacion = LocalDateTime.now();
    }

    public String getNumeroCredencial() {
        return idCredencial != null ? String.valueOf(idCredencial) : null;
    }
}
