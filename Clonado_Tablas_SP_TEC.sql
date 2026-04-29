/*
  Guia de clonado a sufijo _tec
  Proyecto: Centro de Costos TEC
  Motor: PostgreSQL
*/

/*
  RESUMEN DE CONEXIONES (extraido de context.xml del entorno original)
  -------------------------------------------------------------------
  El sistema original NO usa una sola base de datos.
  Para este flujo, los recursos relevantes son:

  1) jdbc/dbdespensa
    - url: jdbc:postgresql://10.250.193.20:5433/dbdespensa
    - user: postgres
    - password: ver9batim

  2) jdbc/megadbpedido
    - url: jdbc:postgresql://10.250.193.20:5435/dbpedido
    - user: postgres
    - password: lqs

  3) jdbc/pddespensa
    - url: jdbc:postgresql://10.250.193.20:5433/pddespensa
    - user: postgres
    - password: ver9batim

  4) jdbc/dbemis
    - url: jdbc:informix-sqli://10.250.193.56:1543/dbemis:INFORMIXSERVER=emisnet
    - user: informix
    - password: ifxfleetcor

  Nota:
  Este script de clonado _tec esta orientado a tablas PostgreSQL del dominio
  Centro de Costos/Despensa. Si alguna tabla origen vive en otra BD/recurso,
  ejecuta el clonado conectado a esa BD especifica.
*/

/* ============================================================
   TABLAS OBJETIVO (_tec)
   ============================================================ */
-- 1) usuario_tec
-- 2) empleado_tec
-- 3) pedido_tec
-- 4) pedido_detalle_tec
-- 5) tarjeta_tec
-- 6) tarjeta_bitacora_tec
-- 7) grupo_tec
-- 8) grupo_empleado_tec


/* ============================================================
   STORED PROCEDURES OBJETIVO (_tec)
   ============================================================ */
-- 1) sp_crear_pedido_dispersion_tec
-- 2) sp_crear_pedido_stock_tec
-- 3) sp_crear_pedido_tarjeta_tec
-- 4) sp_autorizar_pedido_tec
-- 5) sp_activar_tarjeta_tec
-- 6) sp_cancelar_tarjeta_tec
-- 7) sp_registrar_grupo_tec
-- 8) sp_asignar_empleado_grupo_tec
-- 9) sp_actualizar_empleado_tec


/* ============================================================
   CLONAR TABLAS ORIGINALES -> _tec
   (estructura completa + datos)
   Nota: se usa la primera tabla origen que exista por cada objetivo.
   ============================================================ */
DO $$
DECLARE
  rec RECORD;
  v_origen TEXT;
  v_objetivo_existe BOOLEAN;
BEGIN
  FOR rec IN
    SELECT *
    FROM (
      VALUES
        ('usuario_tec', ARRAY['corpusuarios', 'usuario']),
        ('empleado_tec', ARRAY['tmemp', 'empleado']),
        ('pedido_tec', ARRAY['pedido', 'tpedi_tar']),
        ('pedido_detalle_tec', ARRAY['pedido_detalle', 'detalleprefactura']),
        ('tarjeta_tec', ARRAY['tarjeta']),
        ('tarjeta_bitacora_tec', ARRAY['bitacorareposicion', 'tarjeta_bitacora']),
        ('grupo_tec', ARRAY['direccionesgrupo', 'grupo']),
        ('grupo_empleado_tec', ARRAY['detalledirecciones', 'grupo_empleado'])
    ) AS t(objetivo, candidatos)
  LOOP
    SELECT c
    INTO v_origen
    FROM unnest(rec.candidatos) AS c
    WHERE to_regclass('public.' || c) IS NOT NULL
    LIMIT 1;

    IF v_origen IS NULL THEN
      RAISE NOTICE 'No se encontro tabla origen para % (candidatos: %)', rec.objetivo, rec.candidatos;
      CONTINUE;
    END IF;

    v_objetivo_existe := to_regclass('public.' || rec.objetivo) IS NOT NULL;

    IF NOT v_objetivo_existe THEN
      EXECUTE format(
        'CREATE TABLE public.%I (LIKE public.%I INCLUDING ALL)',
        rec.objetivo,
        v_origen
      );

      EXECUTE format(
        'INSERT INTO public.%I SELECT * FROM public.%I',
        rec.objetivo,
        v_origen
      );

      RAISE NOTICE 'Clonado: % <= %', rec.objetivo, v_origen;
    ELSE
      RAISE NOTICE 'Se omite %, ya existe.', rec.objetivo;
    END IF;
  END LOOP;
END $$;


/* ============================================================
   COBERTURA FALTANTE DE FUNCIONALIDAD (CERTIFICACION)
   BASES ADICIONALES: pddespensa, megadbpedido, dbemis
   ============================================================ */

/*
  IMPORTANTE:
  - Ejecuta cada bloque conectado a su base correspondiente.
  - En PostgreSQL se usa CREATE TABLE ... AS SELECT ...
  - En Informix se usa el formato solicitado: SELECT ... INTO TABLE ...
*/


/* ============================================================
   A) pddespensa (PostgreSQL)
   Conexion: jdbc:postgresql://10.250.193.20:5433/pddespensa
   Funcionalidades: pedidos dispersion/stock/tarjeta, reposicion, cancelacion
   ============================================================ */

-- Tablas principales usadas por funcionalidades del sistema original
CREATE TABLE IF NOT EXISTS tpedi_tar_tec AS SELECT * FROM tpedi_tar WITH NO DATA;
INSERT INTO tpedi_tar_tec SELECT * FROM tpedi_tar;

CREATE TABLE IF NOT EXISTS tsepe_tar_tec AS SELECT * FROM tsepe_tar WITH NO DATA;
INSERT INTO tsepe_tar_tec SELECT * FROM tsepe_tar;

CREATE TABLE IF NOT EXISTS tmemp_tec AS SELECT * FROM tmemp WITH NO DATA;
INSERT INTO tmemp_tec SELECT * FROM tmemp;

CREATE TABLE IF NOT EXISTS tcuen_tec AS SELECT * FROM tcuen WITH NO DATA;
INSERT INTO tcuen_tec SELECT * FROM tcuen;

CREATE TABLE IF NOT EXISTS tdisp_tec AS SELECT * FROM tdisp WITH NO DATA;
INSERT INTO tdisp_tec SELECT * FROM tdisp;

CREATE TABLE IF NOT EXISTS ttadi_tec AS SELECT * FROM ttadi WITH NO DATA;
INSERT INTO ttadi_tec SELECT * FROM ttadi;

CREATE TABLE IF NOT EXISTS iva_tec AS SELECT * FROM iva WITH NO DATA;
INSERT INTO iva_tec SELECT * FROM iva;

CREATE TABLE IF NOT EXISTS tctbm_tec AS SELECT * FROM tctbm WITH NO DATA;
INSERT INTO tctbm_tec SELECT * FROM tctbm;

CREATE TABLE IF NOT EXISTS pedidopendientepago_tec AS SELECT * FROM pedidopendientepago WITH NO DATA;
INSERT INTO pedidopendientepago_tec SELECT * FROM pedidopendientepago;

CREATE TABLE IF NOT EXISTS relacionpedidos_tec AS SELECT * FROM relacionpedidos WITH NO DATA;
INSERT INTO relacionpedidos_tec SELECT * FROM relacionpedidos;

-- Clonado de funciones/procedimientos usados desde pddespensa
DO $$
DECLARE
  r RECORD;
  def_text TEXT;
  new_def TEXT;
BEGIN
  FOR r IN
    SELECT p.oid, n.nspname, p.proname
    FROM pg_proc p
    JOIN pg_namespace n ON n.oid = p.pronamespace
    WHERE n.nspname = 'public'
      AND p.proname IN (
        'cc_asignacion_stock',
        'cc_reposicion_stock',
        'cc_cancelar_pedido_desp',
        'cc_cancelaciondefinitiva'
      )
  LOOP
    def_text := pg_get_functiondef(r.oid);
    new_def := regexp_replace(
      def_text,
      'CREATE OR REPLACE (FUNCTION|PROCEDURE)\\s+' || quote_ident(r.nspname) || '\\.' || quote_ident(r.proname) || '\\s*\\(',
      'CREATE OR REPLACE \\1 ' || quote_ident(r.nspname) || '.' || quote_ident(r.proname || '_tec') || '(',
      'i'
    );
    EXECUTE new_def;
  END LOOP;
END $$;


/* ============================================================
   B) megadbpedido (PostgreSQL)
   Conexion: jdbc:postgresql://10.250.193.20:5435/dbpedido
   Funcionalidades: prefactura, pedido, detalle, autorizacion, estado de cuenta
   ============================================================ */

CREATE TABLE IF NOT EXISTS pedido_tec AS SELECT * FROM pedido WITH NO DATA;
INSERT INTO pedido_tec SELECT * FROM pedido;

CREATE TABLE IF NOT EXISTS prefactura_tec AS SELECT * FROM prefactura WITH NO DATA;
INSERT INTO prefactura_tec SELECT * FROM prefactura;

CREATE TABLE IF NOT EXISTS desgloseprefactura_tec AS SELECT * FROM desgloseprefactura WITH NO DATA;
INSERT INTO desgloseprefactura_tec SELECT * FROM desgloseprefactura;

CREATE TABLE IF NOT EXISTS detalleprefactura_tec AS SELECT * FROM detalleprefactura WITH NO DATA;
INSERT INTO detalleprefactura_tec SELECT * FROM detalleprefactura;

CREATE TABLE IF NOT EXISTS ajusteprefactura_tec AS SELECT * FROM ajusteprefactura WITH NO DATA;
INSERT INTO ajusteprefactura_tec SELECT * FROM ajusteprefactura;

CREATE TABLE IF NOT EXISTS edocuentapendiente_tec AS SELECT * FROM edocuentapendiente WITH NO DATA;
INSERT INTO edocuentapendiente_tec SELECT * FROM edocuentapendiente;

CREATE TABLE IF NOT EXISTS concepto_tec AS SELECT * FROM concepto WITH NO DATA;
INSERT INTO concepto_tec SELECT * FROM concepto;

CREATE TABLE IF NOT EXISTS producto_tec AS SELECT * FROM producto WITH NO DATA;
INSERT INTO producto_tec SELECT * FROM producto;

CREATE TABLE IF NOT EXISTS consignatario_tec AS SELECT * FROM consignatario WITH NO DATA;
INSERT INTO consignatario_tec SELECT * FROM consignatario;

CREATE TABLE IF NOT EXISTS monedero_tec AS SELECT * FROM monedero WITH NO DATA;
INSERT INTO monedero_tec SELECT * FROM monedero;

-- Funciones/procedimientos relevantes de negocio en megadbpedido
DO $$
DECLARE
  r RECORD;
  def_text TEXT;
  new_def TEXT;
BEGIN
  FOR r IN
    SELECT p.oid, n.nspname, p.proname
    FROM pg_proc p
    JOIN pg_namespace n ON n.oid = p.pronamespace
    WHERE n.nspname = 'public'
      AND p.proname IN (
        'cc_cancelar_pedido_no_migrado',
        'cancelarpedido',
        'efectinet_ingresa_pedido_dispersion_programado'
      )
  LOOP
    def_text := pg_get_functiondef(r.oid);
    new_def := regexp_replace(
      def_text,
      'CREATE OR REPLACE (FUNCTION|PROCEDURE)\\s+' || quote_ident(r.nspname) || '\\.' || quote_ident(r.proname) || '\\s*\\(',
      'CREATE OR REPLACE \\1 ' || quote_ident(r.nspname) || '.' || quote_ident(r.proname || '_tec') || '(',
      'i'
    );
    EXECUTE new_def;
  END LOOP;
END $$;


/* ============================================================
   C) dbemis (Informix)
   Conexion: jdbc:informix-sqli://10.250.193.56:1543/dbemis:INFORMIXSERVER=emisnet
   Funcionalidades: usuarios, empleados, validaciones y consulta cliente
   ============================================================ */

/*
  EJECUTAR ESTE BLOQUE CONECTADO A INFORMIX dbemis
  (Sintaxis solicitada por ti)

  DATABASE dbemis;

  SELECT * FROM corpusuarios INTO TABLE corpusuarios_tec;
  SELECT * FROM tmemp INTO TABLE tmemp_tec;
  SELECT * FROM bitacorareposicion INTO TABLE bitacorareposicion_tec;
  SELECT * FROM tcope INTO TABLE tcope_tec;

  -- SP relevante en dbemis a duplicar de forma manual:
  -- app_consultaclienteinformix -> app_consultaclienteinformix_tec
  -- 1) Extraer DDL del SP original con dbschema o tu cliente SQL
  -- 2) Cambiar nombre a app_consultaclienteinformix_tec
  -- 3) Ajustar referencias internas a tablas _tec cuando aplique
*/


/* ============================================================
   CLONAR SP ORIGINALES -> _tec
   Y CAMBIAR REFERENCIAS INTERNAS DE TABLAS A _tec
   ============================================================ */
DO $$
DECLARE
  r RECORD;
  def_text TEXT;
  new_def TEXT;
BEGIN
  FOR r IN
    SELECT p.oid, n.nspname, p.proname
    FROM pg_proc p
    JOIN pg_namespace n ON n.oid = p.pronamespace
    WHERE n.nspname = 'public'
      AND p.proname IN (
        'sp_crear_pedido_dispersion',
        'sp_crear_pedido_stock',
        'sp_crear_pedido_tarjeta',
        'sp_autorizar_pedido',
        'sp_activar_tarjeta',
        'sp_cancelar_tarjeta',
        'sp_registrar_grupo',
        'sp_asignar_empleado_grupo',
        'sp_actualizar_empleado'
      )
  LOOP
    def_text := pg_get_functiondef(r.oid);

    new_def := def_text;

    new_def := regexp_replace(
      new_def,
      'CREATE OR REPLACE PROCEDURE\\s+' || quote_ident(r.nspname) || '\\.' || quote_ident(r.proname) || '\\s*\\(',
      'CREATE OR REPLACE PROCEDURE ' || quote_ident(r.nspname) || '.' || quote_ident(r.proname || '_tec') || '(',
      'i'
    );

    new_def := regexp_replace(new_def, '\\musuario\\M', 'usuario_tec', 'g');
    new_def := regexp_replace(new_def, '\\mempleado\\M', 'empleado_tec', 'g');
    new_def := regexp_replace(new_def, '\\mpedido_detalle\\M', 'pedido_detalle_tec', 'g');
    new_def := regexp_replace(new_def, '\\mpedido\\M', 'pedido_tec', 'g');
    new_def := regexp_replace(new_def, '\\mtarjeta_bitacora\\M', 'tarjeta_bitacora_tec', 'g');
    new_def := regexp_replace(new_def, '\\mtarjeta\\M', 'tarjeta_tec', 'g');
    new_def := regexp_replace(new_def, '\\mgrupo_empleado\\M', 'grupo_empleado_tec', 'g');
    new_def := regexp_replace(new_def, '\\mgrupo\\M', 'grupo_tec', 'g');

    EXECUTE new_def;
  END LOOP;
END $$;


/* ============================================================
   VERIFICACIONES
   ============================================================ */
-- Tablas _tec creadas
SELECT tablename
FROM pg_tables
WHERE schemaname = 'public'
  AND tablename LIKE '%_tec'
ORDER BY tablename;

-- SP _tec creados
SELECT n.nspname AS schema_name, p.proname
FROM pg_proc p
JOIN pg_namespace n ON n.oid = p.pronamespace
WHERE n.nspname = 'public'
  AND p.prokind = 'p'
  AND p.proname LIKE 'sp%_tec'
ORDER BY p.proname;

/*
  Nota:
  Si algun nombre de tabla original difiere en tu BD (por ejemplo pedido_detalles),
  ajusta ese nombre en este script antes de ejecutar.
*/
