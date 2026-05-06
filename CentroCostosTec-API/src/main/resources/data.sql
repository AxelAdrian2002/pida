-- Datos semilla de NOVAHUB GESTION INTERNA

INSERT INTO empresa_operativa (codigo_empresa, nombre_empresa, activa)
VALUES ('NOVA01', 'NOVAHUB GESTION INTERNA SA DE CV', TRUE)
ON CONFLICT (codigo_empresa) DO NOTHING;

INSERT INTO unidad_operativa (codigo_unidad, codigo_empresa, clienteid, consignatarioid, activa)
VALUES ('OPER-100', 'NOVA01', 1001, 2001, TRUE)
ON CONFLICT (codigo_empresa, codigo_unidad) DO NOTHING;

INSERT INTO usuario_interno (
    usuariousr, usuarionombre, usuariopwd, usuariocorreo, usuarioactivo,
    perfilid, corporativoid, centroid, usuariofechacreacion, usuariofechamodificacion,
    usuariofechaexpirapwd, bitacoraid
)
VALUES
('admin', 'Administrador NOVAHUB', '0192023a7bbd73250516f069df18b500', 'admin@novahub.local', TRUE, 1, 'NOVA01', 'OPER-100', NOW(), NOW(), NOW() + INTERVAL '90 days', 0),
('captura', 'Analista Operativo', '0192023a7bbd73250516f069df18b500', 'captura@novahub.local', TRUE, 2, 'NOVA01', 'OPER-100', NOW(), NOW(), NOW() + INTERVAL '90 days', 0),
('consulta', 'Supervisor Operativo', '0192023a7bbd73250516f069df18b500', 'consulta@novahub.local', TRUE, 3, 'NOVA01', 'OPER-100', NOW(), NOW(), NOW() + INTERVAL '90 days', 0)
ON CONFLICT (corporativoid, usuariousr) DO NOTHING;

INSERT INTO colaborador (tnuec, tnoem, tappa, tapma, tmail, ttele, tnucl, tnuco, tgrup, tbist, tbife)
VALUES
('10001', 'Andrea', 'Lopez', 'Perez', 'andrea.lopez@novahub.local', '5551111111', 1001, 2001, 'EQ-OPERACIONES', 'A', NOW()),
('10002', 'Luis', 'Martinez', 'Ruiz', 'luis.martinez@novahub.local', '5551111112', 1001, 2001, 'EQ-OPERACIONES', 'A', NOW()),
('10003', 'Carla', 'Hernandez', 'Diaz', 'carla.hernandez@novahub.local', '5551111113', 1001, 2001, 'EQ-FINANZAS', 'A', NOW())
ON CONFLICT (tnuec) DO NOTHING;

INSERT INTO equipo_trabajo (grupoid, descripcion, estatus, clienteid, consignatarioid, fechacreacion)
VALUES
('EQ-OPERACIONES', 'Equipo de operaciones internas', TRUE, 1001, 2001, NOW()),
('EQ-FINANZAS', 'Equipo de control financiero', TRUE, 1001, 2001, NOW())
ON CONFLICT (clienteid, consignatarioid, grupoid) DO NOTHING;

INSERT INTO detalle_direccion_grupo (
    iddirecciones, calle, numero, colonia, codigopostal, delegacion, estado,
    nombre, telefono, nombre2, telefono2, horario, observacion
)
SELECT et.iddirecciones, 'Av. Operaciones', '100', 'Centro', '01000', 'Cuauhtemoc', 'CDMX',
       'Andrea Lopez', '5551111111', 'Luis Martinez', '5551111112', '09:00-18:00', 'Acceso principal por recepcion'
FROM equipo_trabajo et
WHERE et.grupoid = 'EQ-OPERACIONES' AND et.clienteid = 1001 AND et.consignatarioid = 2001
ON CONFLICT (iddirecciones) DO NOTHING;

INSERT INTO detalle_direccion_grupo (
    iddirecciones, calle, numero, colonia, codigopostal, delegacion, estado,
    nombre, telefono, nombre2, telefono2, horario, observacion
)
SELECT et.iddirecciones, 'Av. Finanzas', '250', 'Del Valle', '03100', 'Benito Juarez', 'CDMX',
       'Carla Hernandez', '5551111113', 'Mesa de Control', '5551111199', '08:30-17:30', 'Entrega en piso 3 area financiera'
FROM equipo_trabajo et
WHERE et.grupoid = 'EQ-FINANZAS' AND et.clienteid = 1001 AND et.consignatarioid = 2001
ON CONFLICT (iddirecciones) DO NOTHING;

INSERT INTO estado_credencial (parametrosid, parametrosactiva)
VALUES (1, TRUE), (2, FALSE)
ON CONFLICT (parametrosid) DO NOTHING;

INSERT INTO credencial_interna (
    tarjetaid, empleadoid, empleadonombre, cuentaid, parametrosid, tarjetatipo,
    clienteid, consignatarioid, tarjetafechacreacion, tarjetafechamodificacion, tarjetacancelada
)
VALUES
(70000001, '10001', 'Andrea Lopez Perez', 'CTA-10001', 1, 'T', 1001, 2001, NOW(), NOW(), FALSE),
(70000002, '10002', 'Luis Martinez Ruiz', 'CTA-10002', 2, 'A', 1001, 2001, NOW(), NOW(), FALSE)
ON CONFLICT (tarjetaid) DO NOTHING;

INSERT INTO domicilio_fiscal (domicilioid, calle, entrecalle1, entrecalle2, colonia, numerointerior, numeroexterior, codigopostal, pais, ciudad)
VALUES (1, 'Av. Innovacion', 'Calle Norte', 'Calle Sur', 'Centro', '1', '100', '01000', 'MX', 'CDMX')
ON CONFLICT (domicilioid) DO NOTHING;

INSERT INTO datos_fiscales (datosfiscalid, domicilioid, razonsocial, rfc)
VALUES (1, 1, 'NOVAHUB GESTION INTERNA SA DE CV', 'NGI260101AAA')
ON CONFLICT (datosfiscalid) DO NOTHING;

INSERT INTO unidad_fiscal (clienteid, consignatarioid, datosfiscalid)
VALUES (1001, 2001, 1)
ON CONFLICT (clienteid, consignatarioid) DO NOTHING;

INSERT INTO servicio_catalogo (productoid, nombre)
VALUES
(1, 'Solicitud de Apoyo Economico'),
(2, 'Solicitud de Reposicion de Saldo'),
(3, 'Solicitud de Asignacion Interna')
ON CONFLICT (productoid) DO NOTHING;

INSERT INTO balance_operativo (clienteid, consignatarioid, monederosaldo, creditosaldo, monederomodooperacion, movimientoid)
VALUES (1001, 2001, 150000.00, 50000.00, 'P', 0)
ON CONFLICT (clienteid, consignatarioid) DO NOTHING;

INSERT INTO resumen_operativo (
    prefacturaid, clienteid, consignatarioid, datosfiscalid, servicioid, total, fechacreacion, fechamodificacion, activo
)
VALUES (1, 1001, 2001, 1, 'NHB', 3500.00, NOW(), NOW(), TRUE)
ON CONFLICT (prefacturaid) DO NOTHING;

INSERT INTO solicitud_operativa (
    solicitudid, confirmacionid, facturaid, prefacturaid, estadosolicitudid, clienteid, consignatarioid,
    solicitudmigrada, movimientoid, solicitudtipo, comentario, fechacreacion, fechamodificacion, activo
)
VALUES (1, 1000, -1, 1, 'NVO', 1001, 2001, FALSE, -1, 'APOYO_ECONOMICO', 'N/D', NOW(), NOW(), TRUE)
ON CONFLICT (solicitudid) DO NOTHING;

INSERT INTO desglose_resumen_operativo (
    desgloseid, prefacturaid, clienteid, consignatarioid, productoid, valoriva,
    valorbase, valorcuotaiva, valorfacial, subtotal, cuotaiva, total, activo
)
VALUES (1, 1, 1001, 2001, 1, 16, 3000, 480, 0, 3000, 480, 3480, TRUE)
ON CONFLICT (desgloseid) DO NOTHING;

INSERT INTO detalle_resumen_operativo (
    detalleid, desgloseid, tipoconceptoid, conceptoid, nombrecorto, preciounitario, cantidad, subtotal
)
VALUES (1, 1, 'SRV', 101, 'Servicio Operativo Interno', 3000, 1, 3000)
ON CONFLICT (detalleid) DO NOTHING;

-- =========================================================
-- Carga masiva adicional para ambiente local de pruebas
-- =========================================================

INSERT INTO unidad_operativa (codigo_unidad, codigo_empresa, clienteid, consignatarioid, activa)
VALUES ('OPER-200', 'NOVA01', 1002, 2002, TRUE)
ON CONFLICT (codigo_empresa, codigo_unidad) DO NOTHING;

INSERT INTO usuario_interno (
    usuariousr, usuarionombre, usuariopwd, usuariocorreo, usuarioactivo,
    perfilid, corporativoid, centroid, usuariofechacreacion, usuariofechamodificacion,
    usuariofechaexpirapwd, bitacoraid
)
SELECT
    'operador' || lpad(gs::text, 2, '0'),
    'Operador ' || gs,
    '0192023a7bbd73250516f069df18b500',
    'operador' || lpad(gs::text, 2, '0') || '@novahub.local',
    TRUE,
    CASE WHEN gs % 3 = 0 THEN 3 WHEN gs % 2 = 0 THEN 2 ELSE 1 END,
    'NOVA01',
    CASE WHEN gs % 5 = 0 THEN 'OPER-200' ELSE 'OPER-100' END,
    NOW(), NOW(), NOW() + INTERVAL '90 days', 0
FROM generate_series(1, 20) gs
ON CONFLICT (corporativoid, usuariousr) DO NOTHING;

INSERT INTO colaborador (tnuec, tnoem, tappa, tapma, tmail, ttele, tnucl, tnuco, tgrup, tbist, tbife)
SELECT
    lpad((10000 + gs)::text, 5, '0'),
    'Nombre' || gs,
    'ApellidoP' || gs,
    'ApellidoM' || gs,
    'colaborador' || gs || '@novahub.local',
    '5552' || lpad(gs::text, 6, '0'),
    CASE WHEN gs <= 60 THEN 1001 ELSE 1002 END,
    CASE WHEN gs <= 60 THEN 2001 ELSE 2002 END,
    CASE
        WHEN gs % 6 = 0 THEN 'EQ-IT'
        WHEN gs % 5 = 0 THEN 'EQ-RH'
        WHEN gs % 4 = 0 THEN 'EQ-FINANZAS'
        WHEN gs % 3 = 0 THEN 'EQ-COMPRAS'
        WHEN gs % 2 = 0 THEN 'EQ-LOGISTICA'
        ELSE 'EQ-OPERACIONES'
    END,
    'A',
    NOW()
FROM generate_series(4, 80) gs
ON CONFLICT (tnuec) DO NOTHING;

INSERT INTO equipo_trabajo (grupoid, descripcion, estatus, clienteid, consignatarioid, fechacreacion)
VALUES
('EQ-RH', 'Equipo de recursos humanos', TRUE, 1001, 2001, NOW()),
('EQ-IT', 'Equipo de tecnologia e infraestructura', TRUE, 1001, 2001, NOW()),
('EQ-COMPRAS', 'Equipo de compras internas', TRUE, 1001, 2001, NOW()),
('EQ-LOGISTICA', 'Equipo de logistica y distribucion', TRUE, 1001, 2001, NOW()),
('EQ-SUCURSAL', 'Equipo operativo sucursal', TRUE, 1002, 2002, NOW())
ON CONFLICT (clienteid, consignatarioid, grupoid) DO NOTHING;

INSERT INTO detalle_direccion_grupo (
    iddirecciones, calle, numero, colonia, codigopostal, delegacion, estado,
    nombre, telefono, nombre2, telefono2, horario, observacion
)
SELECT et.iddirecciones,
       'Av. ' || replace(et.grupoid, 'EQ-', ''),
       (100 + et.iddirecciones)::text,
       'Zona Corporativa',
       '01' || lpad((et.iddirecciones % 1000)::text, 3, '0'),
       'Alcaldia Central',
       'CDMX',
       'Contacto ' || et.grupoid,
       '5553' || lpad((et.iddirecciones % 1000000)::text, 6, '0'),
       'Mesa ' || et.grupoid,
       '5554' || lpad((et.iddirecciones % 1000000)::text, 6, '0'),
       '08:00-18:00',
       'Recepcion principal de ' || et.grupoid
FROM equipo_trabajo et
ON CONFLICT (iddirecciones) DO NOTHING;

INSERT INTO equipo_colaborador (id_grupo, id_empleado, numero_empleado, activo, fecha_asignacion, usuario_asigno)
SELECT
    et.iddirecciones,
    c.colaborador_id,
    c.tnuec,
    TRUE,
    NOW(),
    'seed-masivo'
FROM colaborador c
JOIN equipo_trabajo et
    ON et.grupoid = c.tgrup
   AND et.clienteid = c.tnucl
   AND et.consignatarioid = c.tnuco
ON CONFLICT DO NOTHING;

INSERT INTO estado_credencial (parametrosid, parametrosactiva)
SELECT gs, CASE WHEN gs % 3 = 0 THEN FALSE ELSE TRUE END
FROM generate_series(3, 40) gs
ON CONFLICT (parametrosid) DO NOTHING;

INSERT INTO credencial_interna (
    tarjetaid, empleadoid, empleadonombre, cuentaid, parametrosid, tarjetatipo,
    clienteid, consignatarioid, tarjetafechacreacion, tarjetafechamodificacion, tarjetacancelada
)
SELECT
    70000000 + row_number() OVER (ORDER BY c.tnuec),
    c.tnuec,
    trim(c.tnoem || ' ' || c.tappa || ' ' || COALESCE(c.tapma, '')),
    'CTA-' || c.tnuec,
    ((row_number() OVER (ORDER BY c.tnuec) - 1) % 40) + 1,
    CASE WHEN (row_number() OVER (ORDER BY c.tnuec)) % 2 = 0 THEN 'T' ELSE 'A' END,
    c.tnucl,
    c.tnuco,
    NOW() - ((row_number() OVER (ORDER BY c.tnuec)) || ' days')::interval,
    NOW(),
    CASE WHEN (row_number() OVER (ORDER BY c.tnuec)) % 17 = 0 THEN TRUE ELSE FALSE END
FROM colaborador c
WHERE c.tnuec BETWEEN '10001' AND '10080'
ON CONFLICT (tarjetaid) DO NOTHING;

INSERT INTO bitacora_credencial (tarjetaid, estatusanterior, estatusid, usuarioid, usuario_operacion, observacion, fecha_operacion)
SELECT
    ci.tarjetaid,
    'INACTIVA',
    CASE WHEN ec.parametrosactiva THEN 'ACTIVA' ELSE 'INACTIVA' END,
    1,
    'seed-masivo',
    'Registro inicial de estado',
    NOW() - ((ci.tarjetaid % 30) || ' days')::interval
FROM credencial_interna ci
LEFT JOIN estado_credencial ec ON ec.parametrosid = ci.parametrosid
WHERE ci.tarjetaid >= 70000003
ON CONFLICT DO NOTHING;

INSERT INTO domicilio_fiscal (domicilioid, calle, entrecalle1, entrecalle2, colonia, numerointerior, numeroexterior, codigopostal, pais, ciudad)
SELECT
    gs,
    'Calle Fiscal ' || gs,
    'Entre A' || gs,
    'Entre B' || gs,
    'Colonia ' || gs,
    'INT-' || gs,
    (100 + gs)::text,
    '03' || lpad(gs::text, 3, '0'),
    'MX',
    CASE WHEN gs % 2 = 0 THEN 'CDMX' ELSE 'Guadalajara' END
FROM generate_series(2, 25) gs
ON CONFLICT (domicilioid) DO NOTHING;

INSERT INTO datos_fiscales (datosfiscalid, domicilioid, razonsocial, rfc)
SELECT
    gs,
    gs,
    'Razon Social ' || gs,
    'RFC' || lpad(gs::text, 8, '0')
FROM generate_series(2, 25) gs
ON CONFLICT (datosfiscalid) DO NOTHING;

INSERT INTO unidad_fiscal (clienteid, consignatarioid, datosfiscalid)
VALUES (1002, 2002, 2)
ON CONFLICT (clienteid, consignatarioid) DO NOTHING;

INSERT INTO servicio_catalogo (productoid, nombre)
VALUES
(4, 'Solicitud de Stock de Credenciales'),
(5, 'Solicitud de Asignacion Adicional'),
(6, 'Solicitud de Reemplazo por Dano'),
(7, 'Solicitud de Actualizacion de Datos'),
(8, 'Solicitud de Dispersion Programada'),
(9, 'Solicitud de Reactivacion'),
(10, 'Solicitud de Bloqueo Temporal')
ON CONFLICT (productoid) DO NOTHING;

INSERT INTO balance_operativo (clienteid, consignatarioid, monederosaldo, creditosaldo, monederomodooperacion, movimientoid)
VALUES (1002, 2002, 98000.00, 21000.00, 'P', 0)
ON CONFLICT (clienteid, consignatarioid) DO NOTHING;

INSERT INTO resumen_operativo (
    prefacturaid, clienteid, consignatarioid, datosfiscalid, servicioid, total, fechacreacion, fechamodificacion, activo
)
SELECT
    gs,
    CASE WHEN gs % 7 = 0 THEN 1002 ELSE 1001 END,
    CASE WHEN gs % 7 = 0 THEN 2002 ELSE 2001 END,
    CASE WHEN gs % 7 = 0 THEN 2 ELSE 1 END,
    'NHB',
    (1200 + gs * 75)::numeric(14,2),
    NOW() - (gs || ' days')::interval,
    NOW() - (gs || ' days')::interval,
    TRUE
FROM generate_series(2, 120) gs
ON CONFLICT (prefacturaid) DO NOTHING;

INSERT INTO solicitud_operativa (
    solicitudid, confirmacionid, facturaid, prefacturaid, estadosolicitudid, clienteid, consignatarioid,
    solicitudmigrada, movimientoid, solicitudtipo, comentario, fechacreacion, fechamodificacion, activo
)
SELECT
    gs,
    1000 + gs,
    -1,
    gs,
    CASE WHEN gs % 5 = 0 THEN 'AUT' WHEN gs % 4 = 0 THEN 'REV' ELSE 'NVO' END,
    CASE WHEN gs % 7 = 0 THEN 1002 ELSE 1001 END,
    CASE WHEN gs % 7 = 0 THEN 2002 ELSE 2001 END,
    FALSE,
    -1,
    CASE
        WHEN gs % 4 = 0 THEN 'REPOSICION'
        WHEN gs % 3 = 0 THEN 'ASIGNACION_ADICIONAL'
        WHEN gs % 2 = 0 THEN 'STOCK'
        ELSE 'APOYO_ECONOMICO'
    END,
    'Solicitud generada por semilla ' || gs,
    NOW() - (gs || ' days')::interval,
    NOW() - ((gs - 1) || ' days')::interval,
    TRUE
FROM generate_series(2, 120) gs
ON CONFLICT (solicitudid) DO NOTHING;

INSERT INTO desglose_resumen_operativo (
    desgloseid, prefacturaid, clienteid, consignatarioid, productoid, valoriva,
    valorbase, valorcuotaiva, valorfacial, subtotal, cuotaiva, total, activo
)
SELECT
    gs,
    gs,
    CASE WHEN gs % 7 = 0 THEN 1002 ELSE 1001 END,
    CASE WHEN gs % 7 = 0 THEN 2002 ELSE 2001 END,
    ((gs - 1) % 10) + 1,
    16,
    (1000 + gs * 40)::numeric(14,2),
    (160 + gs * 6)::numeric(14,2),
    0,
    (1000 + gs * 40)::numeric(14,2),
    (160 + gs * 6)::numeric(14,2),
    (1160 + gs * 46)::numeric(14,2),
    TRUE
FROM generate_series(2, 120) gs
ON CONFLICT (desgloseid) DO NOTHING;

INSERT INTO detalle_resumen_operativo (
    detalleid, desgloseid, tipoconceptoid, conceptoid, nombrecorto, preciounitario, cantidad, subtotal
)
SELECT
    gs,
    gs,
    'SRV',
    100 + gs,
    'Concepto operativo ' || gs,
    (500 + gs * 10)::numeric(14,2),
    CASE WHEN gs % 3 = 0 THEN 3 WHEN gs % 2 = 0 THEN 2 ELSE 1 END,
    ((500 + gs * 10) * (CASE WHEN gs % 3 = 0 THEN 3 WHEN gs % 2 = 0 THEN 2 ELSE 1 END))::numeric(14,2)
FROM generate_series(2, 120) gs
ON CONFLICT (detalleid) DO NOTHING;

-- Sincronizar secuencias para evitar colisiones tras inserts con IDs explícitos.
SELECT setval(pg_get_serial_sequence('empresa_operativa', 'empresa_id'), COALESCE((SELECT MAX(empresa_id) FROM empresa_operativa), 1), true);
SELECT setval(pg_get_serial_sequence('unidad_operativa', 'unidad_id'), COALESCE((SELECT MAX(unidad_id) FROM unidad_operativa), 1), true);
SELECT setval(pg_get_serial_sequence('usuario_interno', 'usuarioid'), COALESCE((SELECT MAX(usuarioid) FROM usuario_interno), 1), true);
SELECT setval(pg_get_serial_sequence('colaborador', 'colaborador_id'), COALESCE((SELECT MAX(colaborador_id) FROM colaborador), 1), true);
SELECT setval(pg_get_serial_sequence('equipo_trabajo', 'iddirecciones'), COALESCE((SELECT MAX(iddirecciones) FROM equipo_trabajo), 1), true);
SELECT setval(pg_get_serial_sequence('equipo_colaborador', 'id_asignacion'), COALESCE((SELECT MAX(id_asignacion) FROM equipo_colaborador), 1), true);
SELECT setval(pg_get_serial_sequence('estado_credencial', 'parametrosid'), COALESCE((SELECT MAX(parametrosid) FROM estado_credencial), 1), true);
SELECT setval(pg_get_serial_sequence('bitacora_credencial', 'bitacoraid'), COALESCE((SELECT MAX(bitacoraid) FROM bitacora_credencial), 1), true);
SELECT setval(pg_get_serial_sequence('domicilio_fiscal', 'domicilioid'), COALESCE((SELECT MAX(domicilioid) FROM domicilio_fiscal), 1), true);
SELECT setval(pg_get_serial_sequence('datos_fiscales', 'datosfiscalid'), COALESCE((SELECT MAX(datosfiscalid) FROM datos_fiscales), 1), true);
SELECT setval(pg_get_serial_sequence('resumen_operativo', 'prefacturaid'), COALESCE((SELECT MAX(prefacturaid) FROM resumen_operativo), 1), true);
SELECT setval(pg_get_serial_sequence('solicitud_operativa', 'solicitudid'), COALESCE((SELECT MAX(solicitudid) FROM solicitud_operativa), 1), true);
SELECT setval(pg_get_serial_sequence('desglose_resumen_operativo', 'desgloseid'), COALESCE((SELECT MAX(desgloseid) FROM desglose_resumen_operativo), 1), true);
SELECT setval(pg_get_serial_sequence('detalle_resumen_operativo', 'detalleid'), COALESCE((SELECT MAX(detalleid) FROM detalle_resumen_operativo), 1), true);
