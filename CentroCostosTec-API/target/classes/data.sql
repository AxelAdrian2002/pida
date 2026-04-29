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
