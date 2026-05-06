-- Esquema local de NOVAHUB GESTION INTERNA
-- Giro: Plataforma de gestion operativa de solicitudes internas,
-- credenciales de colaboradores y equipos de trabajo.

-- Limpieza de artefactos legacy para migrar a modelo de solicitudes.
DROP VIEW IF EXISTS pedido CASCADE;
DROP VIEW IF EXISTS prefactura CASCADE;
DROP VIEW IF EXISTS desgloseprefactura CASCADE;
DROP VIEW IF EXISTS detalleprefactura CASCADE;
DROP VIEW IF EXISTS producto CASCADE;
DROP VIEW IF EXISTS monedero CASCADE;
DROP VIEW IF EXISTS consignatario CASCADE;
DROP VIEW IF EXISTS datosfiscal CASCADE;
DROP VIEW IF EXISTS domicilio CASCADE;
DROP VIEW IF EXISTS bitacorareposicion CASCADE;
DROP VIEW IF EXISTS tarjeta CASCADE;
DROP VIEW IF EXISTS parametros CASCADE;
DROP VIEW IF EXISTS grupo_empleado_tec CASCADE;
DROP VIEW IF EXISTS direccionesgrupo CASCADE;
DROP VIEW IF EXISTS detalledirecciones CASCADE;
DROP VIEW IF EXISTS tmemp CASCADE;
DROP VIEW IF EXISTS tcope CASCADE;
DROP VIEW IF EXISTS corpusuarios CASCADE;
DROP VIEW IF EXISTS centrocostos CASCADE;
DROP VIEW IF EXISTS corporativos CASCADE;

DROP TABLE IF EXISTS pedido CASCADE;
DROP TABLE IF EXISTS prefactura CASCADE;
DROP TABLE IF EXISTS desgloseprefactura CASCADE;
DROP TABLE IF EXISTS detalleprefactura CASCADE;
DROP TABLE IF EXISTS producto CASCADE;
DROP TABLE IF EXISTS monedero CASCADE;
DROP TABLE IF EXISTS consignatario CASCADE;
DROP TABLE IF EXISTS datosfiscal CASCADE;
DROP TABLE IF EXISTS domicilio CASCADE;
DROP TABLE IF EXISTS bitacorareposicion CASCADE;
DROP TABLE IF EXISTS tarjeta CASCADE;
DROP TABLE IF EXISTS parametros CASCADE;
DROP TABLE IF EXISTS grupo_empleado_tec CASCADE;
DROP TABLE IF EXISTS direccionesgrupo CASCADE;
DROP TABLE IF EXISTS detalledirecciones CASCADE;
DROP TABLE IF EXISTS tmemp CASCADE;
DROP TABLE IF EXISTS tcope CASCADE;
DROP TABLE IF EXISTS corpusuarios CASCADE;
DROP TABLE IF EXISTS centrocostos CASCADE;
DROP TABLE IF EXISTS corporativos CASCADE;

CREATE SEQUENCE IF NOT EXISTS seq_solicitud_confirmacion START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS thepe_tar_tnupp_seq START WITH 1000 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS empresa_operativa (
    empresa_id BIGSERIAL PRIMARY KEY,
    codigo_empresa VARCHAR(20) NOT NULL UNIQUE,
    nombre_empresa VARCHAR(180) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS unidad_operativa (
    unidad_id BIGSERIAL PRIMARY KEY,
    codigo_unidad VARCHAR(20) NOT NULL,
    codigo_empresa VARCHAR(20) NOT NULL,
    clienteid BIGINT NOT NULL,
    consignatarioid BIGINT NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (codigo_empresa, codigo_unidad)
);

CREATE TABLE IF NOT EXISTS usuario_interno (
    usuarioid BIGSERIAL PRIMARY KEY,
    usuariousr VARCHAR(50) NOT NULL,
    usuarionombre VARCHAR(150),
    usuariopwd VARCHAR(255) NOT NULL,
    usuariocorreo VARCHAR(120),
    usuarioactivo BOOLEAN NOT NULL DEFAULT TRUE,
    perfilid INTEGER NOT NULL DEFAULT 1,
    corporativoid VARCHAR(20) NOT NULL,
    centroid VARCHAR(20) NOT NULL,
    usuariofechacreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuariofechamodificacion TIMESTAMP,
    usuariofechaexpirapwd TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    bitacoraid INTEGER NOT NULL DEFAULT 0,
    UNIQUE (corporativoid, usuariousr)
);

CREATE TABLE IF NOT EXISTS colaborador (
    colaborador_id BIGSERIAL PRIMARY KEY,
    tnuec VARCHAR(20) NOT NULL UNIQUE,
    tnoem VARCHAR(100) NOT NULL,
    tappa VARCHAR(100) NOT NULL,
    tapma VARCHAR(100),
    tmail VARCHAR(120),
    ttele VARCHAR(30),
    tnucl BIGINT NOT NULL,
    tnuco BIGINT NOT NULL,
    tgrup VARCHAR(100),
    tbist VARCHAR(1) NOT NULL DEFAULT 'A',
    tbife TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS equipo_trabajo (
    iddirecciones BIGSERIAL PRIMARY KEY,
    grupoid VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    estatus BOOLEAN NOT NULL DEFAULT TRUE,
    clienteid BIGINT,
    consignatarioid BIGINT,
    fechacreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechamodificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (clienteid, consignatarioid, grupoid)
);

CREATE TABLE IF NOT EXISTS detalle_direccion_grupo (
    iddirecciones BIGINT PRIMARY KEY REFERENCES equipo_trabajo(iddirecciones) ON DELETE CASCADE,
    calle VARCHAR(150),
    numero VARCHAR(30),
    colonia VARCHAR(120),
    codigopostal VARCHAR(10),
    delegacion VARCHAR(120),
    estado VARCHAR(120),
    nombre VARCHAR(150),
    telefono VARCHAR(40),
    nombre2 VARCHAR(150),
    telefono2 VARCHAR(40),
    horario VARCHAR(120),
    observacion VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS equipo_colaborador (
    id_asignacion BIGSERIAL PRIMARY KEY,
    id_grupo BIGINT NOT NULL,
    id_empleado BIGINT NOT NULL,
    numero_empleado VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_asigno VARCHAR(80)
);

CREATE TABLE IF NOT EXISTS estado_credencial (
    parametrosid BIGSERIAL PRIMARY KEY,
    parametrosactiva BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS credencial_interna (
    tarjetaid BIGINT PRIMARY KEY,
    empleadoid VARCHAR(20) NOT NULL,
    empleadonombre VARCHAR(200) NOT NULL,
    cuentaid VARCHAR(30) NOT NULL,
    parametrosid BIGINT NOT NULL REFERENCES estado_credencial(parametrosid),
    tarjetatipo VARCHAR(1) NOT NULL DEFAULT 'T',
    clienteid BIGINT NOT NULL,
    consignatarioid BIGINT NOT NULL,
    tarjetafechacreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tarjetafechamodificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tarjetacancelada BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS bitacora_credencial (
    bitacoraid BIGSERIAL PRIMARY KEY,
    tarjetaid BIGINT NOT NULL,
    estatusanterior VARCHAR(20),
    estatusid VARCHAR(20) NOT NULL,
    usuarioid BIGINT,
    usuario_operacion VARCHAR(50),
    observacion VARCHAR(300),
    fecha_operacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS domicilio_fiscal (
    domicilioid BIGSERIAL PRIMARY KEY,
    calle VARCHAR(120),
    entrecalle1 VARCHAR(120),
    entrecalle2 VARCHAR(120),
    colonia VARCHAR(120),
    numerointerior VARCHAR(20),
    numeroexterior VARCHAR(20),
    codigopostal VARCHAR(10),
    pais VARCHAR(80),
    ciudad VARCHAR(80)
);

CREATE TABLE IF NOT EXISTS datos_fiscales (
    datosfiscalid BIGSERIAL PRIMARY KEY,
    domicilioid BIGINT NOT NULL REFERENCES domicilio_fiscal(domicilioid),
    razonsocial VARCHAR(180),
    rfc VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS unidad_fiscal (
    clienteid BIGINT NOT NULL,
    consignatarioid BIGINT NOT NULL,
    datosfiscalid BIGINT NOT NULL REFERENCES datos_fiscales(datosfiscalid),
    PRIMARY KEY (clienteid, consignatarioid)
);

CREATE TABLE IF NOT EXISTS servicio_catalogo (
    productoid BIGINT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS balance_operativo (
    clienteid BIGINT NOT NULL,
    consignatarioid BIGINT NOT NULL,
    monederosaldo NUMERIC(14,2) NOT NULL DEFAULT 0,
    creditosaldo NUMERIC(14,2) NOT NULL DEFAULT 0,
    monederomodooperacion VARCHAR(2) NOT NULL DEFAULT 'P',
    movimientoid BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (clienteid, consignatarioid)
);

CREATE TABLE IF NOT EXISTS resumen_operativo (
    prefacturaid BIGSERIAL PRIMARY KEY,
    clienteid BIGINT NOT NULL,
    consignatarioid BIGINT NOT NULL,
    datosfiscalid BIGINT NOT NULL,
    servicioid VARCHAR(10) NOT NULL DEFAULT 'NHB',
    total NUMERIC(14,2) NOT NULL DEFAULT 0,
    fechacreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechamodificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS solicitud_operativa (
    solicitudid BIGSERIAL PRIMARY KEY,
    confirmacionid BIGINT NOT NULL,
    facturaid BIGINT NOT NULL DEFAULT -1,
    prefacturaid BIGINT NOT NULL REFERENCES resumen_operativo(prefacturaid),
    estadosolicitudid VARCHAR(10) NOT NULL DEFAULT 'NVO',
    clienteid BIGINT NOT NULL,
    consignatarioid BIGINT NOT NULL,
    solicitudmigrada BOOLEAN NOT NULL DEFAULT FALSE,
    movimientoid BIGINT NOT NULL DEFAULT -1,
    solicitudtipo VARCHAR(40) NOT NULL DEFAULT 'APOYO_INTERNO',
    comentario VARCHAR(300) NOT NULL DEFAULT 'N/D',
    fechacreacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechamodificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS desglose_resumen_operativo (
    desgloseid BIGSERIAL PRIMARY KEY,
    prefacturaid BIGINT NOT NULL REFERENCES resumen_operativo(prefacturaid),
    clienteid BIGINT,
    consignatarioid BIGINT,
    productoid BIGINT,
    valoriva NUMERIC(6,2) NOT NULL DEFAULT 16,
    valorbase NUMERIC(14,2) NOT NULL DEFAULT 0,
    valorcuotaiva NUMERIC(14,2) NOT NULL DEFAULT 0,
    valorfacial NUMERIC(14,2) NOT NULL DEFAULT 0,
    subtotal NUMERIC(14,2) NOT NULL DEFAULT 0,
    cuotaiva NUMERIC(14,2) NOT NULL DEFAULT 0,
    total NUMERIC(14,2) NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS detalle_resumen_operativo (
    detalleid BIGSERIAL PRIMARY KEY,
    desgloseid BIGINT NOT NULL REFERENCES desglose_resumen_operativo(desgloseid),
    tipoconceptoid VARCHAR(10),
    conceptoid BIGINT,
    nombrecorto VARCHAR(120),
    preciounitario NUMERIC(14,2) NOT NULL DEFAULT 0,
    cantidad BIGINT NOT NULL DEFAULT 1,
    subtotal NUMERIC(14,2) NOT NULL DEFAULT 0
);

-- Vistas de compatibilidad para evitar romper el backend actual.
CREATE OR REPLACE VIEW corporativos AS
SELECT codigo_empresa AS corporativoid, activa AS corporativoactivo
FROM empresa_operativa;

CREATE OR REPLACE VIEW centrocostos AS
SELECT codigo_unidad AS centroid, codigo_empresa AS corporativoid, clienteid, consignatarioid, activa AS centroactivo
FROM unidad_operativa;

CREATE OR REPLACE VIEW corpusuarios AS
SELECT * FROM usuario_interno;

CREATE OR REPLACE VIEW tmemp AS
SELECT colaborador_id AS tidem, tnuec, tnoem, tappa, tapma, tmail, ttele, tnucl, tnuco, tgrup, tbist, tbife
FROM colaborador;

CREATE OR REPLACE VIEW tcope AS
SELECT clienteid AS tnucl, consignatarioid AS tnuco
FROM unidad_operativa;

CREATE OR REPLACE VIEW direccionesgrupo AS
SELECT * FROM equipo_trabajo;

CREATE OR REPLACE VIEW detalledirecciones AS
SELECT * FROM detalle_direccion_grupo;

CREATE OR REPLACE VIEW grupo_empleado_tec AS
SELECT * FROM equipo_colaborador;

CREATE OR REPLACE VIEW parametros AS
SELECT * FROM estado_credencial;

CREATE OR REPLACE VIEW tarjeta AS
SELECT * FROM credencial_interna;

CREATE OR REPLACE VIEW bitacorareposicion AS
SELECT * FROM bitacora_credencial;

CREATE OR REPLACE VIEW domicilio AS
SELECT * FROM domicilio_fiscal;

CREATE OR REPLACE VIEW datosfiscal AS
SELECT * FROM datos_fiscales;

CREATE OR REPLACE VIEW consignatario AS
SELECT * FROM unidad_fiscal;

CREATE OR REPLACE VIEW producto AS
SELECT * FROM servicio_catalogo;

CREATE OR REPLACE VIEW monedero AS
SELECT clienteid, consignatarioid, monederosaldo, creditosaldo, monederomodooperacion, movimientoid
FROM balance_operativo;

CREATE OR REPLACE VIEW prefactura AS
SELECT * FROM resumen_operativo;

CREATE OR REPLACE VIEW pedido AS
SELECT
    solicitudid AS pedidoid,
    confirmacionid,
    facturaid,
    prefacturaid,
    estadosolicitudid AS estadopedidoid,
    clienteid,
    consignatarioid,
    solicitudmigrada AS pedidomigrado,
    movimientoid,
    solicitudtipo AS pedidotipo,
    comentario AS comentarios,
    fechacreacion,
    fechamodificacion,
    activo
FROM solicitud_operativa;

CREATE OR REPLACE VIEW desgloseprefactura AS
SELECT
    desgloseid AS desgloseprefacturaid,
    prefacturaid,
    clienteid,
    consignatarioid,
    productoid,
    valoriva,
    valorbase,
    valorcuotaiva,
    valorfacial,
    subtotal,
    cuotaiva,
    total,
    activo
FROM desglose_resumen_operativo;

CREATE OR REPLACE VIEW detalleprefactura AS
SELECT
    detalleid AS detalleprefacturaid,
    desgloseid AS desgloseprefacturaid,
    tipoconceptoid,
    conceptoid,
    nombrecorto,
    preciounitario,
    cantidad,
    subtotal
FROM detalle_resumen_operativo;
