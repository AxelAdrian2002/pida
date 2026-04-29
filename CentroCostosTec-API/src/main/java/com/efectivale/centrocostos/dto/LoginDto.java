package com.efectivale.centrocostos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {

    @NotBlank(message = "El corporativo es obligatorio")
    private String corporativo;

    @NotBlank(message = "El centro de costos es obligatorio")
    private String centrocostos;

    @NotBlank(message = "El usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contrasena es obligatoria")
    private String password;

    /** Contador de intentos fallidos enviado por el cliente (0-based). */
    private Integer intentos = 0;

    public String getCorporativo() {
        return corporativo;
    }

    public void setCorporativo(String corporativo) {
        this.corporativo = corporativo;
    }

    public String getCentrocostos() {
        return centrocostos;
    }

    public void setCentrocostos(String centrocostos) {
        this.centrocostos = centrocostos;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIntentos() {
        return intentos;
    }

    public void setIntentos(Integer intentos) {
        this.intentos = intentos;
    }
}
