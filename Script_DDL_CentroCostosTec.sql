# Script DDL — Centro de Costos TEC
# Base de datos: dbdespensa (PostgreSQL 10.250.193.20:5433)
# Todas las tablas y SPs llevan sufijo _tec

-- ============================================================
--  TABLAS
-- ============================================================

-- 1. usuario_tec
CREATE TABLE public.usuario_tec (
    id_usuario        SERIAL PRIMARY KEY,
    username          VARCHAR(50)  NOT NULL UNIQUE,
    password          VARCHAR(255) NOT NULL,         -- BCrypt
    email             VARCHAR(100),
    rol               VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN','CAPTURA','CONSULTA','AUTORIZADOR')),
    nombre_completo   VARCHAR(150),
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_alta        TIMESTAMP    NOT NULL DEFAULT NOW(),
    fecha_ultimo_acceso TIMESTAMP
);

-- 2. empleado_tec
CREATE TABLE public.empleado_tec (
    id_empleado         SERIAL PRIMARY KEY,
    numero_empleado     VARCHAR(20)  NOT NULL UNIQUE,
    nombre              VARCHAR(100) NOT NULL,
    apellido_paterno    VARCHAR(100) NOT NULL,
    apellido_materno    VARCHAR(100),
    email               VARCHAR(100),
    telefono            VARCHAR(20),
    departamento        VARCHAR(100),
    puesto              VARCHAR(100),
    activo              BOOLEAN   NOT NULL DEFAULT TRUE,
    fecha_alta          TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    usuario_modificacion VARCHAR(50)
);

-- 3. pedido_tec
CREATE TABLE public.pedido_tec (
    id_pedido         SERIAL PRIMARY KEY,
    tipo_pedido       VARCHAR(20)  NOT NULL CHECK (tipo_pedido IN ('DISPERSION','STOCK','TARJETA')),
    estado            VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE'
                        CHECK (estado IN ('PENDIENTE','AUTORIZADO','RECHAZADO','CANCELADO')),
    monto_total       NUMERIC(15,2),
    id_usuario        INTEGER      NOT NULL REFERENCES usuario_tec(id_usuario),
    descripcion       VARCHAR(255),
    referencia        VARCHAR(100),
    fecha_creacion    TIMESTAMP    NOT NULL DEFAULT NOW(),
    fecha_autorizacion TIMESTAMP,
    usuario_autorizo  VARCHAR(50),
    observaciones     VARCHAR(500)
);

-- 4. pedido_detalle_tec
CREATE TABLE public.pedido_detalle_tec (
    id_detalle       SERIAL PRIMARY KEY,
    id_pedido        INTEGER     NOT NULL REFERENCES pedido_tec(id_pedido),
    id_empleado      INTEGER,
    numero_empleado  VARCHAR(20),
    nombre_empleado  VARCHAR(200),
    monto            NUMERIC(15,2),
    descripcion      VARCHAR(255),
    numero_tarjeta   VARCHAR(30)
);

-- 5. grupo_tec
CREATE TABLE public.grupo_tec (
    id_grupo     SERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL UNIQUE,
    descripcion  VARCHAR(255),
    activo       BOOLEAN   NOT NULL DEFAULT TRUE,
    fecha_alta   TIMESTAMP NOT NULL DEFAULT NOW(),
    usuario_alta VARCHAR(50)
);

-- 6. grupo_empleado_tec
CREATE TABLE public.grupo_empleado_tec (
    id_asignacion    SERIAL PRIMARY KEY,
    id_grupo         INTEGER     NOT NULL REFERENCES grupo_tec(id_grupo),
    id_empleado      INTEGER     NOT NULL,
    numero_empleado  VARCHAR(20) NOT NULL,
    activo           BOOLEAN   NOT NULL DEFAULT TRUE,
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT NOW(),
    usuario_asigno   VARCHAR(50)
);

-- 7. tarjeta_tec
CREATE TABLE public.tarjeta_tec (
    id_tarjeta         SERIAL PRIMARY KEY,
    numero_tarjeta     VARCHAR(30)  NOT NULL UNIQUE,
    id_empleado        INTEGER,
    numero_empleado    VARCHAR(20),
    nombre_empleado    VARCHAR(200),
    estado             VARCHAR(20)  NOT NULL DEFAULT 'INACTIVA'
                         CHECK (estado IN ('ACTIVA','INACTIVA','CANCELADA')),
    id_grupo           INTEGER REFERENCES grupo_tec(id_grupo),
    fecha_emision      DATE,
    fecha_activacion   TIMESTAMP,
    fecha_cancelacion  TIMESTAMP,
    motivo_cancelacion VARCHAR(300),
    usuario_operacion  VARCHAR(50),
    fecha_alta         TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 8. tarjeta_bitacora_tec
CREATE TABLE public.tarjeta_bitacora_tec (
    id_bitacora      SERIAL PRIMARY KEY,
    id_tarjeta       INTEGER      NOT NULL,
    numero_tarjeta   VARCHAR(30),
    estado_anterior  VARCHAR(20),
    estado_nuevo     VARCHAR(20),
    id_usuario       INTEGER,
    usuario_operacion VARCHAR(50),
    observacion      VARCHAR(300),
    fecha_operacion  TIMESTAMP NOT NULL DEFAULT NOW()
);


-- ============================================================
--  STORED PROCEDURES
-- ============================================================

-- SP 1: Crear pedido de dispersión
CREATE OR REPLACE PROCEDURE public.sp_crear_pedido_dispersion_tec(
    p_id_usuario  INTEGER,
    p_descripcion VARCHAR,
    p_referencia  VARCHAR,
    OUT p_resultado VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
    v_id INTEGER;
BEGIN
    INSERT INTO pedido_tec(tipo_pedido, estado, id_usuario, descripcion, referencia)
    VALUES ('DISPERSION', 'PENDIENTE', p_id_usuario, p_descripcion, p_referencia)
    RETURNING id_pedido INTO v_id;

    p_resultado := 'OK:' || v_id;
EXCEPTION WHEN OTHERS THEN
    p_resultado := 'ERROR:' || SQLERRM;
END;
$$;

-- SP 2: Crear pedido de stock
CREATE OR REPLACE PROCEDURE public.sp_crear_pedido_stock_tec(
    p_id_usuario  INTEGER,
    p_descripcion VARCHAR,
    OUT p_resultado VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
    v_id INTEGER;
BEGIN
    INSERT INTO pedido_tec(tipo_pedido, estado, id_usuario, descripcion)
    VALUES ('STOCK', 'PENDIENTE', p_id_usuario, p_descripcion)
    RETURNING id_pedido INTO v_id;

    p_resultado := 'OK:' || v_id;
EXCEPTION WHEN OTHERS THEN
    p_resultado := 'ERROR:' || SQLERRM;
END;
$$;

-- SP 3: Crear pedido de tarjetas
CREATE OR REPLACE PROCEDURE public.sp_crear_pedido_tarjeta_tec(
    p_id_usuario  INTEGER,
    p_descripcion VARCHAR,
    OUT p_resultado VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
    v_id INTEGER;
BEGIN
    INSERT INTO pedido_tec(tipo_pedido, estado, id_usuario, descripcion)
    VALUES ('TARJETA', 'PENDIENTE', p_id_usuario, p_descripcion)
    RETURNING id_pedido INTO v_id;

    p_resultado := 'OK:' || v_id;
EXCEPTION WHEN OTHERS THEN
    p_resultado := 'ERROR:' || SQLERRM;
END;
$$;

-- SP 4: Autorizar pedido
CREATE OR REPLACE PROCEDURE public.sp_autorizar_pedido_tec(
    p_id_pedido  INTEGER,
    p_id_usuario INTEGER,
    OUT p_resultado VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
    v_usuario VARCHAR;
BEGIN
    SELECT username INTO v_usuario FROM usuario_tec WHERE id_usuario = p_id_usuario;

    UPDATE pedido_tec
    SET estado = 'AUTORIZADO',
        fecha_autorizacion = NOW(),
        usuario_autorizo = v_usuario
    WHERE id_pedido = p_id_pedido AND estado = 'PENDIENTE';

    IF NOT FOUND THEN
        p_resultado := 'ERROR:Pedido no encontrado o no está en estado PENDIENTE';
    ELSE
        p_resultado := 'OK';
    END IF;
EXCEPTION WHEN OTHERS THEN
    p_resultado := 'ERROR:' || SQLERRM;
END;
$$;

-- SP 5: Activar tarjeta
CREATE OR REPLACE PROCEDURE public.sp_activar_tarjeta_tec(
    p_numero_tarjeta VARCHAR,
    p_id_usuario     INTEGER,
    OUT p_resultado  VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
    v_usuario VARCHAR;
BEGIN
    SELECT username INTO v_usuario FROM usuario_tec WHERE id_usuario = p_id_usuario;

    UPDATE tarjeta_tec
    SET estado = 'ACTIVA',
        fecha_activacion = NOW(),
        usuario_operacion = v_usuario
    WHERE numero_tarjeta = p_numero_tarjeta AND estado = 'INACTIVA';

    IF NOT FOUND THEN
        p_resultado := 'ERROR:Tarjeta no encontrada o no está en estado INACTIVA';
    ELSE
        p_resultado := 'OK';
    END IF;
EXCEPTION WHEN OTHERS THEN
    p_resultado := 'ERROR:' || SQLERRM;
END;
$$;

-- SP 6: Cancelar tarjeta
CREATE OR REPLACE PROCEDURE public.sp_cancelar_tarjeta_tec(
    p_numero_tarjeta VARCHAR,
    p_id_usuario     INTEGER,
    p_motivo         VARCHAR,
    OUT p_resultado  VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
    v_usuario VARCHAR;
BEGIN
    SELECT username INTO v_usuario FROM usuario_tec WHERE id_usuario = p_id_usuario;

    UPDATE tarjeta_tec
    SET estado = 'CANCELADA',
        fecha_cancelacion = NOW(),
        motivo_cancelacion = p_motivo,
        usuario_operacion = v_usuario
    WHERE numero_tarjeta = p_numero_tarjeta AND estado <> 'CANCELADA';

    IF NOT FOUND THEN
        p_resultado := 'ERROR:Tarjeta no encontrada o ya está cancelada';
    ELSE
        p_resultado := 'OK';
    END IF;
EXCEPTION WHEN OTHERS THEN
    p_resultado := 'ERROR:' || SQLERRM;
END;
$$;

-- SP 7: Registrar grupo
CREATE OR REPLACE PROCEDURE public.sp_registrar_grupo_tec(
    p_nombre      VARCHAR,
    p_descripcion VARCHAR,
    p_usuario     VARCHAR,
    OUT p_resultado VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
    v_id INTEGER;
BEGIN
    IF EXISTS (SELECT 1 FROM grupo_tec WHERE nombre = p_nombre) THEN
        p_resultado := 'ERROR:Ya existe un grupo con ese nombre';
        RETURN;
    END IF;

    INSERT INTO grupo_tec(nombre, descripcion, activo, usuario_alta)
    VALUES (p_nombre, p_descripcion, TRUE, p_usuario)
    RETURNING id_grupo INTO v_id;

    p_resultado := 'OK:' || v_id;
EXCEPTION WHEN OTHERS THEN
    p_resultado := 'ERROR:' || SQLERRM;
END;
$$;

-- SP 8: Asignar empleado a grupo
CREATE OR REPLACE PROCEDURE public.sp_asignar_empleado_grupo_tec(
    p_id_grupo      INTEGER,
    p_id_empleado   INTEGER,
    p_usuario       VARCHAR,
    OUT p_resultado VARCHAR
)
LANGUAGE plpgsql AS $$
DECLARE
    v_numero VARCHAR;
BEGIN
    SELECT numero_empleado INTO v_numero FROM empleado_tec WHERE id_empleado = p_id_empleado;

    IF EXISTS (SELECT 1 FROM grupo_empleado_tec WHERE id_grupo = p_id_grupo AND id_empleado = p_id_empleado AND activo = TRUE) THEN
        p_resultado := 'ERROR:El empleado ya está activo en este grupo';
        RETURN;
    END IF;

    INSERT INTO grupo_empleado_tec(id_grupo, id_empleado, numero_empleado, activo, usuario_asigno)
    VALUES (p_id_grupo, p_id_empleado, v_numero, TRUE, p_usuario);

    p_resultado := 'OK';
EXCEPTION WHEN OTHERS THEN
    p_resultado := 'ERROR:' || SQLERRM;
END;
$$;

-- SP 9: Actualizar datos de empleado
CREATE OR REPLACE PROCEDURE public.sp_actualizar_empleado_tec(
    p_numero_empleado VARCHAR,
    p_email           VARCHAR,
    p_telefono        VARCHAR,
    p_departamento    VARCHAR,
    p_puesto          VARCHAR,
    p_usuario         VARCHAR,
    OUT p_resultado   VARCHAR
)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE empleado_tec
    SET email               = COALESCE(NULLIF(p_email, ''),       email),
        telefono            = COALESCE(NULLIF(p_telefono, ''),    telefono),
        departamento        = COALESCE(NULLIF(p_departamento,''), departamento),
        puesto              = COALESCE(NULLIF(p_puesto, ''),      puesto),
        fecha_modificacion  = NOW(),
        usuario_modificacion = p_usuario
    WHERE numero_empleado = p_numero_empleado;

    IF NOT FOUND THEN
        p_resultado := 'ERROR:Empleado no encontrado';
    ELSE
        p_resultado := 'OK';
    END IF;
EXCEPTION WHEN OTHERS THEN
    p_resultado := 'ERROR:' || SQLERRM;
END;
$$;


-- ============================================================
--  DATOS INICIALES (usuario admin para pruebas)
-- ============================================================
-- Contraseña: Admin2026! (BCrypt generado con strength 10)
INSERT INTO usuario_tec(username, password, email, rol, nombre_completo, activo)
VALUES ('admin',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lA.',
        'admin@efectivale.com.mx', 'ADMIN', 'Administrador TEC', TRUE)
ON CONFLICT DO NOTHING;
