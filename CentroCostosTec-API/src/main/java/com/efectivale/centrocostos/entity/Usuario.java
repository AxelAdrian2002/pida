package com.efectivale.centrocostos.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "corpusuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuarioid")
    private Long idUsuario;

    @Column(name = "usuariousr", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "usuariopwd", nullable = false, length = 255)
    private String password;

    @Column(name = "usuariocorreo", length = 100)
    private String email;

    @Column(name = "perfilid", nullable = false)
    private Integer perfilId;

    @Column(name = "usuarionombre", length = 150)
    private String nombreCompleto;

    @Column(name = "usuarioactivo", nullable = false)
    private Boolean activo = true;

    @Column(name = "usuariofechacreacion", nullable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "usuariofechamodificacion")
    private LocalDateTime fechaUltimoAcceso;

    @Column(name = "corporativoid", nullable = false, length = 20)
    private String corporativoId;

    @Column(name = "centroid", nullable = false, length = 20)
    private String centroId;

    @Column(name = "usuariofechaexpirapwd", nullable = false)
    private LocalDateTime fechaExpiraPwd;

    @Column(name = "bitacoraid", nullable = false)
    private Integer bitacoraId;

    @Transient
    private String rol;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaAlta == null) {
            fechaAlta = now;
        }
        if (fechaUltimoAcceso == null) {
            fechaUltimoAcceso = now;
        }
        if (fechaExpiraPwd == null) {
            fechaExpiraPwd = now.plusDays(90);
        }
        if (bitacoraId == null) {
            bitacoraId = 0;
        }
    }

    public String getRol() {
        if (rol != null && !rol.isBlank()) {
            return rol;
        }
        return switch (perfilId != null ? perfilId : -1) {
            case 1, 4 -> "ADMIN";
            case 2 -> "CAPTURA";
            case 3 -> "CONSULTA";
            case 5 -> "AUTORIZADOR";
            default -> "CONSULTA";
        };
    }

    public void setRol(String rol) {
        this.rol = rol;
        this.perfilId = switch (rol != null ? rol.toUpperCase() : "") {
            case "ADMIN" -> 1;
            case "CAPTURA" -> 2;
            case "AUTORIZADOR" -> 5;
            case "CONSULTA" -> 3;
            default -> this.perfilId;
        };
    }
}
