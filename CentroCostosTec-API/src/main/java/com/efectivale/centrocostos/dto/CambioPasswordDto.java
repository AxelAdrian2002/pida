package com.efectivale.centrocostos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambioPasswordDto {

    @NotNull(message = "El id de usuario es obligatorio")
    private Long id;

    @NotBlank(message = "La contrasena anterior es obligatoria")
    private String passwordanterior;

    @NotBlank(message = "La nueva contrasena es obligatoria")
    private String nuevoPassword;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPasswordanterior() {
        return passwordanterior;
    }

    public void setPasswordanterior(String passwordanterior) {
        this.passwordanterior = passwordanterior;
    }

    public String getNuevoPassword() {
        return nuevoPassword;
    }

    public void setNuevoPassword(String nuevoPassword) {
        this.nuevoPassword = nuevoPassword;
    }
}
