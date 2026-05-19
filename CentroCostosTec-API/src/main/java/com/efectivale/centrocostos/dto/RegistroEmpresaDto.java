package com.efectivale.centrocostos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroEmpresaDto {

    // ----- Datos de la empresa -----
    @NotBlank
    @Size(max = 180)
    private String nombreEmpresa;

    @NotBlank
    @Size(max = 20)
    private String rfc;

    @Size(max = 180)
    private String razonSocial;

    @Size(max = 20)
    private String colorPrimario;

    @Size(max = 20)
    private String colorSecundario;

    @Size(max = 500)
    private String logoUrl;

    @Email
    @Size(max = 120)
    private String emailEmpresa;

    @Size(max = 30)
    private String telefonoEmpresa;

    @Size(max = 200)
    private String sitioWeb;

    // ----- Dirección -----
    @Size(max = 150)
    private String calle;

    @Size(max = 30)
    private String numeroExterior;

    @Size(max = 30)
    private String numeroInterior;

    @Size(max = 120)
    private String colonia;

    @Size(max = 120)
    private String municipio;

    @Size(max = 120)
    private String estado;

    @Size(max = 80)
    private String pais;

    @Size(max = 10)
    private String codigoPostal;

    // ----- Datos del administrador -----
    @NotBlank
    @Size(max = 150)
    private String adminNombre;

    @NotBlank
    @Email
    @Size(max = 120)
    private String adminEmail;

    @NotBlank
    @Size(min = 6, max = 100)
    private String adminPassword;
}
