package com.efectivale.centrocostos.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int codigo;
    private String mensaje;
    private T datos;

    public ApiResponse() {
    }

    public ApiResponse(int codigo, String mensaje, T datos) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getDatos() {
        return datos;
    }

    public void setDatos(T datos) {
        this.datos = datos;
    }

    public static <T> ApiResponse<T> exito(T datos) {
        return new ApiResponse<>(200, "Operacion exitosa", datos);
    }

    public static <T> ApiResponse<T> exito(String mensaje, T datos) {
        return new ApiResponse<>(200, mensaje, datos);
    }

    public static <T> ApiResponse<T> creado(T datos) {
        return new ApiResponse<>(201, "Registro creado exitosamente", datos);
    }

    public static <T> ApiResponse<T> error(int codigo, String mensaje) {
        return new ApiResponse<>(codigo, mensaje, null);
    }
}
