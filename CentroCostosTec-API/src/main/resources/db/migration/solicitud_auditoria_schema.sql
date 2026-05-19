-- Script de creación de tabla de auditoría para solicitudes
-- Ejecutar en: megadbpedido

CREATE TABLE IF NOT EXISTS solicitud_auditoria (
    id SERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    consignatario_id BIGINT NOT NULL,
    usuario_id BIGINT,
    usuario_nombre VARCHAR(255),
    accion VARCHAR(50) NOT NULL, -- CREAR, AUTORIZAR, RECHAZAR, CANCELAR
    tipo_solicitud VARCHAR(50), -- DISPERSION, STOCK, TARJETA, ADICIONAL
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50),
    motivo_cambio TEXT,
    datos_anteriores TEXT, -- JSON
    datos_nuevos TEXT, -- JSON
    fecha_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    direccion_ip VARCHAR(45),
    activo BOOLEAN DEFAULT true,
    CONSTRAINT fk_solicitud_auditoria_pedido FOREIGN KEY (solicitud_id) REFERENCES pedido(pedidoid) ON DELETE CASCADE
);

-- Crear índices para mejor rendimiento
CREATE INDEX idx_solicitud_auditoria_solicitud_id ON solicitud_auditoria(solicitud_id);
CREATE INDEX idx_solicitud_auditoria_cliente_consignatario ON solicitud_auditoria(cliente_id, consignatario_id);
CREATE INDEX idx_solicitud_auditoria_fecha ON solicitud_auditoria(fecha_cambio);
CREATE INDEX idx_solicitud_auditoria_accion ON solicitud_auditoria(accion);

-- Comentarios para documentación
COMMENT ON TABLE solicitud_auditoria IS 'Tabla de auditoría que registra todos los cambios de estado de solicitudes';
COMMENT ON COLUMN solicitud_auditoria.id IS 'ID único del registro de auditoría';
COMMENT ON COLUMN solicitud_auditoria.solicitud_id IS 'ID de la solicitud (referencia a pedido.pedidoid)';
COMMENT ON COLUMN solicitud_auditoria.cliente_id IS 'ID del cliente para multi-tenencia';
COMMENT ON COLUMN solicitud_auditoria.consignatario_id IS 'ID del consignatario para multi-tenencia';
COMMENT ON COLUMN solicitud_auditoria.usuario_id IS 'ID del usuario que realizó la acción';
COMMENT ON COLUMN solicitud_auditoria.usuario_nombre IS 'Nombre del usuario que realizó la acción';
COMMENT ON COLUMN solicitud_auditoria.accion IS 'Tipo de acción: CREAR, AUTORIZAR, RECHAZAR, CANCELAR';
COMMENT ON COLUMN solicitud_auditoria.tipo_solicitud IS 'Tipo de solicitud: DISPERSION, STOCK, TARJETA, ADICIONAL';
COMMENT ON COLUMN solicitud_auditoria.estado_anterior IS 'Estado anterior de la solicitud';
COMMENT ON COLUMN solicitud_auditoria.estado_nuevo IS 'Estado nuevo de la solicitud';
COMMENT ON COLUMN solicitud_auditoria.motivo_cambio IS 'Motivo del cambio (ej: observaciones, motivo de rechazo)';
COMMENT ON COLUMN solicitud_auditoria.datos_anteriores IS 'JSON con valores anteriores';
COMMENT ON COLUMN solicitud_auditoria.datos_nuevos IS 'JSON con valores nuevos';
COMMENT ON COLUMN solicitud_auditoria.fecha_cambio IS 'Fecha y hora del cambio';
COMMENT ON COLUMN solicitud_auditoria.direccion_ip IS 'Dirección IP de la solicitud HTTP (opcional)';
