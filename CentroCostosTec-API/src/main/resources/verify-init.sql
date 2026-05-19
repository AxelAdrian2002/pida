-- Verificacion rapida de inicializacion de base
-- Ejecutar despues de schema.sql y data.sql

SELECT 'empresa_operativa' AS tabla, COUNT(*) AS total FROM empresa_operativa
UNION ALL
SELECT 'unidad_operativa', COUNT(*) FROM unidad_operativa
UNION ALL
SELECT 'usuario_interno', COUNT(*) FROM usuario_interno
UNION ALL
SELECT 'colaborador', COUNT(*) FROM colaborador
UNION ALL
SELECT 'equipo_trabajo', COUNT(*) FROM equipo_trabajo
UNION ALL
SELECT 'estado_credencial', COUNT(*) FROM estado_credencial
UNION ALL
SELECT 'credencial_interna', COUNT(*) FROM credencial_interna
UNION ALL
SELECT 'domicilio_fiscal', COUNT(*) FROM domicilio_fiscal
UNION ALL
SELECT 'datos_fiscales', COUNT(*) FROM datos_fiscales
UNION ALL
SELECT 'unidad_fiscal', COUNT(*) FROM unidad_fiscal
UNION ALL
SELECT 'servicio_catalogo', COUNT(*) FROM servicio_catalogo
UNION ALL
SELECT 'balance_operativo', COUNT(*) FROM balance_operativo
UNION ALL
SELECT 'resumen_operativo', COUNT(*) FROM resumen_operativo
UNION ALL
SELECT 'solicitud_operativa', COUNT(*) FROM solicitud_operativa;

-- Debe existir el mapeo fiscal de pruebas que usan varios endpoints
SELECT clienteid, consignatarioid, datosfiscalid
FROM unidad_fiscal
WHERE (clienteid, consignatarioid) IN ((1001,2001),(1,1),(1002,2002))
ORDER BY clienteid, consignatarioid;

-- Verifica vistas legacy claves
SELECT 'pedido' AS vista, COUNT(*) AS total FROM pedido
UNION ALL
SELECT 'prefactura', COUNT(*) FROM prefactura
UNION ALL
SELECT 'monedero', COUNT(*) FROM monedero
UNION ALL
SELECT 'tarjeta', COUNT(*) FROM tarjeta;
