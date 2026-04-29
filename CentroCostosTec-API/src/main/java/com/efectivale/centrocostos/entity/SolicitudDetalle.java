package com.efectivale.centrocostos.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SolicitudDetalle {

    private Long idDetalle;
    private Long idEmpleado;
    private String numeroEmpleado;
    private String nombreEmpleado;
    private BigDecimal monto;
    private String descripcion;
    private String numeroCredencial;
}
