package com.efectivale.centrocostos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "solicitud_auditoria", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudAuditoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "solicitud_id")
    private Long solicitudId;
    
    @Column(name = "cliente_id")
    private Long clienteId;
    
    @Column(name = "consignatario_id")
    private Long consignatarioId;
    
    @Column(name = "usuario_id")
    private Long usuarioId;
    
    @Column(name = "usuario_nombre")
    private String usuarioNombre;
    
    @Column(name = "accion")
    private String accion; // CREAR, AUTORIZAR, RECHAZAR, CANCELAR
    
    @Column(name = "tipo_solicitud")
    private String tipoSolicitud; // DISPERSION, STOCK, TARJETA, ADICIONAL
    
    @Column(name = "estado_anterior")
    private String estadoAnterior;
    
    @Column(name = "estado_nuevo")
    private String estadoNuevo;
    
    @Column(name = "motivo_cambio", columnDefinition = "TEXT")
    private String motivoCambio; // Observación, rechazo, etc
    
    @Column(name = "datos_anteriores", columnDefinition = "TEXT")
    private String datosAnteriores; // JSON de valores anteriores
    
    @Column(name = "datos_nuevos", columnDefinition = "TEXT")
    private String datosNuevos; // JSON de valores nuevos
    
    @Column(name = "fecha_cambio")
    private LocalDateTime fechaCambio;
    
    @Column(name = "direccion_ip")
    private String direccionIP;
    
    @Column(name = "activo")
    private Boolean activo = true;
}
