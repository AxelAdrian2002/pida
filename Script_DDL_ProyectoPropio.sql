-- Proyecto propio: Plataforma de apoyos internos
-- Motor objetivo: PostgreSQL

CREATE TABLE IF NOT EXISTS area (
    area_id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS colaborador (
    colaborador_id BIGSERIAL PRIMARY KEY,
    numero_colaborador VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    apellido_paterno VARCHAR(120) NOT NULL,
    apellido_materno VARCHAR(120),
    email VARCHAR(180),
    telefono_fijo VARCHAR(30),
    extension VARCHAR(15),
    telefono_movil VARCHAR(30),
    puesto VARCHAR(120),
    area_id BIGINT REFERENCES area(area_id),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS equipo (
    equipo_id BIGSERIAL PRIMARY KEY,
    clave VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(300),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS equipo_colaborador (
    equipo_colaborador_id BIGSERIAL PRIMARY KEY,
    equipo_id BIGINT NOT NULL REFERENCES equipo(equipo_id),
    colaborador_id BIGINT NOT NULL REFERENCES colaborador(colaborador_id),
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_asignacion VARCHAR(80) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (equipo_id, colaborador_id)
);

CREATE TABLE IF NOT EXISTS catalogo_estado_solicitud (
    estado_id VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL
);

INSERT INTO catalogo_estado_solicitud (estado_id, nombre) VALUES
('NVO', 'Nueva'),
('REV', 'En revision'),
('APR', 'Aprobada'),
('REJ', 'Rechazada'),
('CAN', 'Cancelada')
ON CONFLICT (estado_id) DO NOTHING;

CREATE TABLE IF NOT EXISTS solicitud_apoyo (
    solicitud_id BIGSERIAL PRIMARY KEY,
    folio BIGINT NOT NULL UNIQUE,
    tipo_solicitud VARCHAR(40) NOT NULL,
    estado_id VARCHAR(20) NOT NULL REFERENCES catalogo_estado_solicitud(estado_id),
    area_id BIGINT REFERENCES area(area_id),
    equipo_id BIGINT REFERENCES equipo(equipo_id),
    descripcion VARCHAR(300) NOT NULL DEFAULT 'N/D',
    monto_total NUMERIC(14,2) NOT NULL DEFAULT 0,
    usuario_creacion VARCHAR(80) NOT NULL,
    usuario_modificacion VARCHAR(80),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS solicitud_detalle (
    solicitud_detalle_id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitud_apoyo(solicitud_id),
    colaborador_id BIGINT NOT NULL REFERENCES colaborador(colaborador_id),
    monto NUMERIC(14,2) NOT NULL DEFAULT 0,
    descripcion VARCHAR(250) NOT NULL DEFAULT 'N/D',
    referencia VARCHAR(60),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS credencial_interna (
    credencial_id BIGSERIAL PRIMARY KEY,
    colaborador_id BIGINT NOT NULL REFERENCES colaborador(colaborador_id),
    numero_credencial VARCHAR(40) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'INACTIVA',
    fecha_activacion TIMESTAMP,
    fecha_baja TIMESTAMP,
    motivo_baja VARCHAR(250),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bitacora_estado_credencial (
    bitacora_id BIGSERIAL PRIMARY KEY,
    credencial_id BIGINT NOT NULL REFERENCES credencial_interna(credencial_id),
    estado_anterior VARCHAR(20),
    estado_nuevo VARCHAR(20) NOT NULL,
    comentario VARCHAR(250) NOT NULL DEFAULT 'N/D',
    usuario VARCHAR(80) NOT NULL,
    fecha_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_solicitud_estado ON solicitud_apoyo (estado_id);
CREATE INDEX IF NOT EXISTS idx_solicitud_tipo ON solicitud_apoyo (tipo_solicitud);
CREATE INDEX IF NOT EXISTS idx_detalle_solicitud ON solicitud_detalle (solicitud_id);
CREATE INDEX IF NOT EXISTS idx_credencial_colaborador ON credencial_interna (colaborador_id);

-- =========================
-- DATOS SEMILLA
-- =========================

INSERT INTO area (area_id, nombre, activo)
VALUES
(1, 'Operaciones', TRUE),
(2, 'Finanzas', TRUE),
(3, 'Recursos Humanos', TRUE)
ON CONFLICT (area_id) DO NOTHING;

INSERT INTO colaborador (
    colaborador_id, numero_colaborador, nombre, apellido_paterno, apellido_materno,
    email, telefono_fijo, extension, telefono_movil, puesto, area_id, activo
)
VALUES
(1, 'COL-10001', 'Andrea', 'Lopez', 'Perez', 'andrea.lopez@local.test', '5551111111', '101', '5557000001', 'Analista', 1, TRUE),
(2, 'COL-10002', 'Luis', 'Martinez', 'Ruiz', 'luis.martinez@local.test', '5551111112', '102', '5557000002', 'Supervisor', 1, TRUE),
(3, 'COL-10003', 'Carla', 'Hernandez', 'Diaz', 'carla.hernandez@local.test', '5551111113', '201', '5557000003', 'Especialista', 2, TRUE)
ON CONFLICT (numero_colaborador) DO NOTHING;

INSERT INTO equipo (equipo_id, clave, nombre, descripcion, activo)
VALUES
(1, 'EQ-OPS', 'Equipo Operaciones', 'Gestión de operación interna', TRUE),
(2, 'EQ-FIN', 'Equipo Finanzas', 'Gestión financiera interna', TRUE)
ON CONFLICT (clave) DO NOTHING;

INSERT INTO equipo_colaborador (equipo_id, colaborador_id, usuario_asignacion, activo)
VALUES
(1, 1, 'admin', TRUE),
(1, 2, 'admin', TRUE),
(2, 3, 'admin', TRUE)
ON CONFLICT (equipo_id, colaborador_id) DO NOTHING;

INSERT INTO solicitud_apoyo (
    solicitud_id, folio, tipo_solicitud, estado_id, area_id, equipo_id,
    descripcion, monto_total, usuario_creacion, usuario_modificacion, activo
)
VALUES
(1, 20260001, 'APOYO_ECONOMICO', 'NVO', 1, 1, 'N/D', 3500.00, 'admin', 'admin', TRUE),
(2, 20260002, 'REPOSICION_SALDO', 'APR', 2, 2, 'N/D', 1800.00, 'admin', 'admin', TRUE)
ON CONFLICT (folio) DO NOTHING;

INSERT INTO solicitud_detalle (
    solicitud_id, colaborador_id, monto, descripcion, referencia
)
VALUES
(1, 1, 2000.00, 'N/D', 'REF-OPS-01'),
(1, 2, 1500.00, 'N/D', 'REF-OPS-02'),
(2, 3, 1800.00, 'N/D', 'REF-FIN-01');

INSERT INTO credencial_interna (
    credencial_id, colaborador_id, numero_credencial, estado, fecha_activacion,
    fecha_baja, motivo_baja, activo
)
VALUES
(1, 1, 'CRD-7000001', 'ACTIVA', NOW(), NULL, NULL, TRUE),
(2, 2, 'CRD-7000002', 'INACTIVA', NULL, NULL, NULL, TRUE),
(3, 3, 'CRD-7000003', 'BAJA', NOW() - INTERVAL '20 days', NOW() - INTERVAL '2 days', 'Reposicion', TRUE)
ON CONFLICT (numero_credencial) DO NOTHING;

INSERT INTO bitacora_estado_credencial (
    credencial_id, estado_anterior, estado_nuevo, comentario, usuario
)
VALUES
(1, 'INACTIVA', 'ACTIVA', 'Activacion inicial', 'admin'),
(3, 'ACTIVA', 'BAJA', 'Baja por reposicion', 'admin');
