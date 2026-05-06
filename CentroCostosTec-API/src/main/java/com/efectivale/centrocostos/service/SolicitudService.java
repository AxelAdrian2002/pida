package com.efectivale.centrocostos.service;




import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efectivale.centrocostos.dto.SolicitudDto;
import com.efectivale.centrocostos.entity.Solicitud;
import com.efectivale.centrocostos.entity.SolicitudDetalle;
import com.efectivale.centrocostos.security.ContextProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudService.class);
    private final @Qualifier("megadbpedidoJdbc") JdbcTemplate megadbpedidoJdbc;
    private final @Qualifier("pddespensaJdbc") JdbcTemplate pddespensaJdbc;
    private final MultiDbBusinessService multiDbBusinessService;
    private final ContextProvider contextProvider;

    // ------- SOLICITUD DISPERSION -------
    @Transactional
    public Solicitud crearSolicitudDispersion(SolicitudDto dto) {
        log.info("Creando solicitud dispersion para usuario {}", dto.getIdUsuario());
        validarTipoSolicitud(dto.getTipoSolicitud(), "DISPERSION");
        multiDbBusinessService.validarContextoSolicitud(dto, "DISPERSION");

        return guardarSolicitud(dto, "DISPERSION");
    }

    // ------- SOLICITUD STOCK -------
    @Transactional
    public Solicitud crearSolicitudStock(SolicitudDto dto) {
        log.info("Creando solicitud stock para usuario {}", dto.getIdUsuario());
        validarTipoSolicitud(dto.getTipoSolicitud(), "STOCK");
        multiDbBusinessService.validarContextoSolicitud(dto, "STOCK");

        return guardarSolicitud(dto, "STOCK");
    }

    // ------- SOLICITUD NUEVA ASIGNACION -------
    @Transactional
    public Solicitud crearSolicitudAsignacion(SolicitudDto dto) {
        log.info("Creando solicitud nueva asignacion para usuario {}", dto.getIdUsuario());
        validarTipoSolicitud(dto.getTipoSolicitud(), "TARJETA");
        multiDbBusinessService.validarContextoSolicitud(dto, "TARJETA");

        return guardarSolicitud(dto, "TARJETA");
    }

    // ------- SOLICITUD ADICIONAL -------
    @Transactional
    public Solicitud crearSolicitudAdicional(SolicitudDto dto) {
        log.info("Creando solicitud adicional para usuario {}", dto.getIdUsuario());
        validarTipoSolicitud(dto.getTipoSolicitud(), "ADICIONAL");
        multiDbBusinessService.validarContextoSolicitud(dto, "ADICIONAL");

        return guardarSolicitud(dto, "ADICIONAL");
    }

    // ------- AUTORIZAR SOLICITUD -------
    @Transactional
    public Solicitud autorizarSolicitud(Long idSolicitud, Long idUsuarioAutoriza, String observaciones) {
        log.info("Autorizando solicitud {} por usuario {}", idSolicitud, idUsuarioAutoriza);

        Solicitud solicitud = obtenerSolicitud(idSolicitud);

        SolicitudDto contexto = new SolicitudDto();
        contexto.setIdUsuario(idUsuarioAutoriza);
        contexto.setClienteId(solicitud.getClienteId());
        contexto.setConsignatarioId(solicitud.getConsignatarioId());
        multiDbBusinessService.validarContextoSolicitud(contexto, "AUTORIZACION");

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden autorizar solicitudes en estado PENDIENTE");
        }

        solicitud.setEstado("AUTORIZADO");
        solicitud.setFechaAutorizacion(LocalDateTime.now());
        solicitud.setObservaciones(observaciones);
        solicitud.setDescripcion(observaciones != null && !observaciones.isBlank() ? observaciones : "N/D");

        int actualizados = megadbpedidoJdbc.update(
                "UPDATE pedido SET estadopedidoid = ?, fechamodificacion = ?, comentarios = ? WHERE pedidoid = ? AND activo = TRUE",
                solicitud.getEstadoId(),
                Timestamp.valueOf(LocalDateTime.now()),
                solicitud.getDescripcion(),
                idSolicitud
        );

        if (actualizados != 1) {
            throw new IllegalStateException("No fue posible autorizar la solicitud " + idSolicitud);
        }

        return solicitud;
    }

    // ------- CONSULTAS -------
    public Page<Solicitud> consultarSolicitudes(String estado, Pageable pageable) {
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();
        
        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de cliente/consignatario de la sesiÃ³n");
        }
        
        StringBuilder where = new StringBuilder(" WHERE p.activo = TRUE AND p.clienteid = ? AND p.consignatarioid = ? ");
        List<Object> params = new ArrayList<>();
        params.add(clienteId);
        params.add(consignatarioId);

        if (estado != null && !estado.isBlank()) {
            List<String> estados = mapearEstado(estado);
            if (!estados.isEmpty()) {
                where.append(" AND p.estadopedidoid IN (");
                where.append("?,".repeat(estados.size()));
                where.setLength(where.length() - 1);
                where.append(")");
                params.addAll(estados);
            }
        }

        Long total = megadbpedidoJdbc.queryForObject(
                "SELECT COUNT(*) FROM pedido p" + where,
                Long.class,
                params.toArray()
        );

            String sql = "SELECT p.pedidoid, p.confirmacionid, p.estadopedidoid, p.clienteid, p.consignatarioid, p.pedidomigrado, p.movimientoid, p.pedidotipo, p.comentarios, p.fechacreacion, p.fechamodificacion, p.activo, COALESCE(pf.total, 0) AS montototal " +
                "FROM pedido p LEFT JOIN prefactura pf ON pf.prefacturaid = p.prefacturaid AND pf.clienteid = p.clienteid AND pf.consignatarioid = p.consignatarioid" + where + " ORDER BY p.pedidoid DESC LIMIT ? OFFSET ?";

        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(pageable.getPageSize());
        queryParams.add((int) pageable.getOffset());

        List<Solicitud> solicitudes = megadbpedidoJdbc.query(sql, (rs, rowNum) -> mapearSolicitud(rs), queryParams.toArray());
        return new PageImpl<>(solicitudes, pageable, total != null ? total : 0);
    }

    public Solicitud obtenerSolicitud(Long idSolicitud) {
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();
        
        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de cliente/consignatario de la sesiÃ³n");
        }
        
        SqlRowSet rs = megadbpedidoJdbc.queryForRowSet(
            "SELECT p.pedidoid, p.confirmacionid, p.estadopedidoid, p.clienteid, p.consignatarioid, p.pedidomigrado, p.movimientoid, p.pedidotipo, p.comentarios, p.fechacreacion, p.fechamodificacion, p.activo, COALESCE(pf.total, 0) AS montototal " +
                "FROM pedido p LEFT JOIN prefactura pf ON pf.prefacturaid = p.prefacturaid AND pf.clienteid = p.clienteid AND pf.consignatarioid = p.consignatarioid " +
                "WHERE p.pedidoid = ? AND p.activo = TRUE AND p.clienteid = ? AND p.consignatarioid = ?",
                idSolicitud, clienteId, consignatarioId
        );
        if (!rs.next()) {
            throw new IllegalArgumentException("Solicitud no encontrada: " + idSolicitud);
        }
        return mapearSolicitud(rs);
    }

    public Map<String, BigDecimal> obtenerSaldoMonedero(Long clienteId, Long consignatarioId) {
        if (clienteId == null || consignatarioId == null) {
            return Map.of("monederoSaldo", BigDecimal.ZERO, "creditoSaldo", BigDecimal.ZERO);
        }

        SqlRowSet rs = megadbpedidoJdbc.queryForRowSet(
                "SELECT monederosaldo, creditosaldo FROM monedero WHERE clienteid = ? AND consignatarioid = ?",
                clienteId,
                consignatarioId
        );

        if (!rs.next()) {
            megadbpedidoJdbc.update(
                    "INSERT INTO monedero (clienteid, consignatarioid, monederomodooperacion, monederosaldo, movimientoid, creditosaldo) VALUES (?, ?, 'P', 0, 0, 0)",
                    clienteId,
                    consignatarioId
            );
            return Map.of("monederoSaldo", BigDecimal.ZERO, "creditoSaldo", BigDecimal.ZERO);
        }

        BigDecimal monederoSaldo = rs.getBigDecimal("monederosaldo");
        BigDecimal creditoSaldo = rs.getBigDecimal("creditosaldo");
        return Map.of(
                "monederoSaldo", monederoSaldo != null ? monederoSaldo : BigDecimal.ZERO,
                "creditoSaldo", creditoSaldo != null ? creditoSaldo : BigDecimal.ZERO
        );
    }

    public Map<String, Object> recuperarVistaPrefacturaGuardado(Long pedidoId, Long prefacturaIdHint) {
        if ((pedidoId == null || pedidoId <= 0) && (prefacturaIdHint == null || prefacturaIdHint <= 0)) {
            throw new IllegalArgumentException("Se requiere pedidoId o prefacturaId para recuperar la prefactura");
        }

        Map<String, Object> pedido = new LinkedHashMap<>();

        if (pedidoId != null && pedidoId > 0) {
            List<Map<String, Object>> pedidos = megadbpedidoJdbc.queryForList(
                    "SELECT " +
                            "p.pedidoid AS pedidoid, " +
                            "p.prefacturaid AS prefacturaid, " +
                            "p.clienteid AS clienteid, " +
                            "p.consignatarioid AS consignatarioid, " +
                            "p.confirmacionid AS confirmacionid, " +
                            "p.pedidotipo AS pedidotipo, " +
                            "p.comentarios AS comentarios, " +
                            "COALESCE(pf.total, 0) AS montototal " +
                            "FROM pedido p " +
                            "LEFT JOIN prefactura pf ON pf.prefacturaid = p.prefacturaid " +
                            "AND pf.clienteid = p.clienteid " +
                            "AND pf.consignatarioid = p.consignatarioid " +
                            "WHERE p.pedidoid = ? AND p.activo = TRUE " +
                            "ORDER BY p.pedidoid DESC LIMIT 1",
                    pedidoId
            );
            if (!pedidos.isEmpty()) {
                pedido.putAll(pedidos.get(0));
            }
        }

        Long prefacturaId = asLong(pedido.get("prefacturaid"));
        if (prefacturaId == null || prefacturaId <= 0) {
            prefacturaId = prefacturaIdHint;
        }

        Long clienteId = asLong(pedido.get("clienteid"));
        Long consignatarioId = asLong(pedido.get("consignatarioid"));
        BigDecimal montoTotal = asBigDecimal(pedido.get("montototal"));

        if ((clienteId == null || consignatarioId == null) && prefacturaId != null && prefacturaId > 0) {
            List<Map<String, Object>> pref = megadbpedidoJdbc.queryForList(
                    "SELECT clienteid, consignatarioid, total FROM prefactura WHERE prefacturaid = ? LIMIT 1",
                    prefacturaId
            );
            if (!pref.isEmpty()) {
                Map<String, Object> p = pref.get(0);
                if (clienteId == null) {
                    clienteId = asLong(p.get("clienteid"));
                }
                if (consignatarioId == null) {
                    consignatarioId = asLong(p.get("consignatarioid"));
                }
                if (montoTotal == null) {
                    montoTotal = asBigDecimal(p.get("total"));
                }
            }
        }

        Long productoId = mapearProductoPorTipoPedido(asString(pedido.get("pedidotipo")));
        Map<String, Object> prefactura = construirPrefacturaCompleta(
                prefacturaId,
                clienteId,
                consignatarioId,
                "DESP",
                montoTotal,
                productoId,
                List.of()
        );

        Map<String, Object> vista = new LinkedHashMap<>();
        vista.put("pedidoid", pedidoId != null ? pedidoId : 0L);
        vista.put("clienteid", clienteId != null ? clienteId : 0L);
        vista.put("consignatarioid", consignatarioId != null ? consignatarioId : 0L);
        vista.put("productoId", productoId != null ? productoId : 0L);
        vista.put("productonombre", resolverNombreProducto(productoId));
        vista.put("montoTotal", montoTotal != null ? montoTotal : BigDecimal.ZERO);
        vista.put("pedidoCliente", true);
        vista.put("referenciasBnacarias", List.of());
        vista.put("prefactura", prefactura);
        return vista;
    }

    // ------- UTILIDADES PRIVADAS -------
    private void validarTipoSolicitud(String tipoSolicitud, String esperado) {
        if (!esperado.equals(tipoSolicitud)) {
            throw new IllegalArgumentException("Tipo de solicitud invalido. Se esperaba: " + esperado);
        }
    }

    private Solicitud guardarSolicitud(SolicitudDto dto, String tipo) {
        Long confirmacionId = siguienteConfirmacionId();
        LocalDateTime now = LocalDateTime.now();
        String descripcionSolicitud = construirDescripcionSolicitud(tipo);

        Long clienteId = dto.getClienteId() != null ? dto.getClienteId() : 0L;
        Long consignatarioId = dto.getConsignatarioId() != null ? dto.getConsignatarioId() : 0L;

        Long prefacturaId = dto.getPrefacturaId();
        if (prefacturaId == null || prefacturaId <= 0) {
            prefacturaId = registrarPrefactura(clienteId, consignatarioId, dto.getMontoTotal(), "DESP");
        }

        Long idPedido = megadbpedidoJdbc.queryForObject(
                "INSERT INTO pedido (confirmacionid, facturaid, prefacturaid, estadopedidoid, clienteid, consignatarioid, pedidomigrado, movimientoid, pedidotipo, comentarios, fechacreacion, fechamodificacion, activo) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING pedidoid",
                Long.class,
                confirmacionId,
                -1,
                prefacturaId,
                "NVO",
                clienteId,
                consignatarioId,
                false,
                -1,
                tipo,
                descripcionSolicitud,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now),
                true
        );

        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(idPedido);
        solicitud.setConfirmacionId(confirmacionId);
        solicitud.setTipoSolicitud(tipo);
        solicitud.setEstado("PENDIENTE");
        solicitud.setMontoTotal(dto.getMontoTotal());
        solicitud.setIdUsuario(dto.getIdUsuario());
        solicitud.setDescripcion(descripcionSolicitud);
        solicitud.setReferencia(dto.getReferencia());
        solicitud.setFechaCreacion(now);
        solicitud.setClienteId(dto.getClienteId() != null ? dto.getClienteId() : 0);
        solicitud.setConsignatarioId(dto.getConsignatarioId() != null ? dto.getConsignatarioId() : 0);
        solicitud.setMigrado(false);
        solicitud.setMovimientoId(-1L);
        solicitud.setActivo(true);

        if (dto.getDetalles() != null && !dto.getDetalles().isEmpty()) {
            List<SolicitudDetalle> detalles = new ArrayList<>();
            for (SolicitudDto.SolicitudDetalleDto d : dto.getDetalles()) {
                SolicitudDetalle detalle = new SolicitudDetalle();
                detalle.setIdEmpleado(d.getIdEmpleado());
                detalle.setNumeroEmpleado(d.getNumeroEmpleado());
                detalle.setNombreEmpleado(d.getNombreEmpleado());
                detalle.setMonto(d.getMonto());
                detalle.setDescripcion(d.getDescripcion());
                detalle.setNumeroCredencial(d.getNumeroCredencial());
                detalles.add(detalle);
            }
            solicitud.setDetalles(detalles);
        }
        return solicitud;
    }

    private String construirDescripcionSolicitud(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return "Solicitud generada";
        }
        return switch (tipo) {
            case "DISPERSION" -> "Solicitud de dispersion";
            case "STOCK" -> "Solicitud de stock";
            case "TARJETA" -> "Solicitud de nueva asignacion";
            case "ADICIONAL" -> "Solicitud de asignacion adicional";
            default -> "Solicitud de " + tipo.toLowerCase();
        };
    }

    public Long registrarPrefactura(Long clienteId, Long consignatarioId, BigDecimal total, String servicioId) {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalSeguro = total != null ? total : BigDecimal.ZERO;
        String servicioSeguro = (servicioId == null || servicioId.isBlank()) ? "DESP" : servicioId;
        Long clienteSeguro = clienteId != null ? clienteId : 0L;
        Long consignatarioSeguro = consignatarioId != null ? consignatarioId : 0L;

        Long datosFiscalId = resolverDatosFiscalId(clienteSeguro, consignatarioSeguro);

        try {
            return megadbpedidoJdbc.queryForObject(
                    "INSERT INTO prefactura (clienteid, consignatarioid, datosfiscalid, servicioid, total, fechacreacion, fechamodificacion, activo) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING prefacturaid",
                    Long.class,
                    clienteSeguro,
                    consignatarioSeguro,
                    datosFiscalId,
                    servicioSeguro,
                    totalSeguro,
                    Timestamp.valueOf(now),
                    Timestamp.valueOf(now),
                    true
            );
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible registrar la prefactura ", ex);
        }
    }

    private Long resolverDatosFiscalId(Long clienteId, Long consignatarioId) {
        try {
            Long datosFiscalId = megadbpedidoJdbc.query(
                    "SELECT datosfiscalid FROM consignatario WHERE clienteid = ? AND consignatarioid = ? ORDER BY consignatarioid LIMIT 1",
                    rs -> rs.next() ? rs.getLong(1) : null,
                    clienteId,
                    consignatarioId
            );
            if (datosFiscalId != null && datosFiscalId > 0) {
                return datosFiscalId;
            }
        } catch (Exception ignored) {
            // Fallback abajo.
        }

        try {
            Long datosFiscalIdHist = megadbpedidoJdbc.query(
                    "SELECT datosfiscalid FROM prefactura WHERE clienteid = ? AND consignatarioid = ? AND activo = TRUE ORDER BY prefacturaid DESC LIMIT 1",
                    rs -> rs.next() ? rs.getLong(1) : null,
                    clienteId,
                    consignatarioId
            );
            if (datosFiscalIdHist != null && datosFiscalIdHist > 0) {
                return datosFiscalIdHist;
            }
        } catch (Exception ignored) {
            // Si no hay histÃ³rico, se falla con mensaje explÃ­cito.
        }

        throw new IllegalStateException("No se encontrÃ³ datosFiscalId vÃ¡lido para cliente/consignatario");
    }

    private Long siguienteConfirmacionId() {
        Integer confirmacionId = pddespensaJdbc.queryForObject("SELECT nextval('thepe_tar_tnupp_seq')", Integer.class);
        if (confirmacionId == null) {
            throw new IllegalStateException("No fue posible obtener confirmacionId para el pedido");
        }
        return confirmacionId.longValue();
    }

    private List<String> mapearEstado(String estado) {
        return switch (estado.toUpperCase()) {
            case "PENDIENTE" -> List.of("NVO", "FACA", "FACR", "PCRE");
            case "AUTORIZADO" -> List.of("PLIB");
            case "RECHAZADO" -> List.of("RECH");
            case "CANCELADO" -> List.of("CANP", "CANC");
            default -> List.of();
        };
    }

    private Solicitud mapearSolicitud(java.sql.ResultSet rs) throws java.sql.SQLException {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(rs.getLong("pedidoid"));
        solicitud.setConfirmacionId(rs.getLong("confirmacionid"));
        solicitud.setClienteId(rs.getLong("clienteid"));
        solicitud.setConsignatarioId(rs.getLong("consignatarioid"));
        solicitud.setMigrado(rs.getBoolean("pedidomigrado"));
        solicitud.setMovimientoId(rs.getLong("movimientoid"));
        solicitud.setTipoSolicitud(rs.getString("pedidotipo"));
        solicitud.setDescripcion(rs.getString("comentarios"));
        solicitud.setFechaCreacion(rs.getTimestamp("fechacreacion").toLocalDateTime());
        solicitud.setActivo(rs.getBoolean("activo"));
        solicitud.setEstadoId(rs.getString("estadopedidoid"));
        BigDecimal monto = rs.getBigDecimal("montototal");
        solicitud.setMontoTotal(monto != null ? monto : BigDecimal.ZERO);
        return solicitud;
    }

    private Solicitud mapearSolicitud(SqlRowSet rs) {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(rs.getLong("pedidoid"));
        solicitud.setConfirmacionId(rs.getLong("confirmacionid"));
        solicitud.setClienteId(rs.getLong("clienteid"));
        solicitud.setConsignatarioId(rs.getLong("consignatarioid"));
        solicitud.setMigrado(rs.getBoolean("pedidomigrado"));
        solicitud.setMovimientoId(rs.getLong("movimientoid"));
        solicitud.setTipoSolicitud(rs.getString("pedidotipo"));
        solicitud.setDescripcion(rs.getString("comentarios"));
        solicitud.setFechaCreacion(rs.getTimestamp("fechacreacion").toLocalDateTime());
        solicitud.setActivo(rs.getBoolean("activo"));
        solicitud.setEstadoId(rs.getString("estadopedidoid"));
        BigDecimal monto = rs.getBigDecimal("montototal");
        solicitud.setMontoTotal(monto != null ? monto : BigDecimal.ZERO);
        return solicitud;
    }

    public byte[] generarPlantillaDispersion(String data) {
        Long clienteId = resolveContextLong(data, "clienteId", contextProvider.getClienteId());
        Long consignatarioId = resolveContextLong(data, "consignatarioId", contextProvider.getConsignatarioId());
        String centroId = resolveContextString(data, "centroId", null);

        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo resolver cliente/consignatario para generar la plantilla");
        }

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("CREAR PEDIDO");

            Row titulo = sheet.createRow(0);
            titulo.createCell(0).setCellValue("PEDIDO DISPERSION A TARJETA");

            Row meta = sheet.createRow(1);
            meta.createCell(0).setCellValue((centroId != null ? centroId : "CENTRO") + " / " + LocalDateTime.now());

            Row total = sheet.createRow(2);
            total.createCell(0).setCellValue("TOTAL");
            total.createCell(1).setCellFormula("SUM(E5:E50000)");

            Row header = sheet.createRow(3);
            header.createCell(0).setCellValue("Centro Costos");
            header.createCell(1).setCellValue("Cliente");
            header.createCell(2).setCellValue("Tipo Pedido");
            header.createCell(3).setCellValue("Numero");
            header.createCell(4).setCellValue("Importe");

            List<String> empleados = pddespensaJdbc.query(
                    "SELECT tnuec FROM tmemp WHERE tnucl = ? AND tnuco = ? AND tbist = 'A' ORDER BY tidem",
                    (rs, rowNum) -> rs.getString("tnuec"),
                    clienteId.intValue(),
                    consignatarioId.intValue()
            );

            int row = 4;
            for (String empleado : empleados) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(centroId != null ? centroId : "");
                r.createCell(1).setCellValue(clienteId + "-" + consignatarioId);
                r.createCell(2).setCellValue("DISPERSION");
                r.createCell(3).setCellValue(empleado != null ? empleado.trim() : "");
                r.createCell(4).setCellValue(0d);
            }

            for (int i = 0; i <= 5; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible generar la plantilla de dispersion", ex);
        }
    }

    public Map<String, Object> procesarDispersion(String user, String b64, String promociones) {
        Long clienteId = resolveContextLong(user, "clienteId", contextProvider.getClienteId());
        Long consignatarioId = resolveContextLong(user, "consignatarioId", contextProvider.getConsignatarioId());
        Long idUsuario = resolveContextLong(user, "idUsuario", contextProvider.getIdUsuario());

        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo resolver cliente/consignatario de la sesion");
        }
        if (b64 == null || b64.isBlank()) {
            throw new IllegalArgumentException("No se recibio archivo para procesar");
        }

        String normalizado = b64.contains(",") ? b64.substring(b64.indexOf(',') + 1) : b64;
        DataFormatter formatter = new DataFormatter();

        try (ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(normalizado));
             XSSFWorkbook wb = new XSSFWorkbook(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet hoja = wb.getSheetAt(0);
            if (hoja == null) {
                throw new IllegalArgumentException("El archivo no contiene hoja valida");
            }

            Set<String> empleadosUnicos = new LinkedHashSet<>();
            List<Map<String, Object>> detalles = new ArrayList<>();
            double sumaImportes = 0d;
            boolean hayErrores = false;

            for (int idx = 4; idx <= hoja.getLastRowNum(); idx++) {
                Row row = hoja.getRow(idx);
                if (row == null) {
                    continue;
                }

                String numero = cellAsString(row.getCell(3), formatter);
                String clienteCell = cellAsString(row.getCell(1), formatter);
                String tipoPedido = cellAsString(row.getCell(2), formatter).toUpperCase();
                Double importe = cellAsDouble(row.getCell(4), formatter);

                if (isBlank(numero) && (importe == null || importe == 0d)) {
                    continue;
                }

                List<String> errores = new ArrayList<>();
                if (isBlank(clienteCell) || !clienteCell.equals(clienteId + "-" + consignatarioId)) {
                    errores.add("Cliente/consignatario invalido");
                }
                if (!"DISPERSION".equals(tipoPedido)) {
                    errores.add("Tipo pedido invalido");
                }
                if (isBlank(numero)) {
                    errores.add("Numero empleado requerido");
                }
                if (importe == null || importe <= 0d) {
                    errores.add("Importe invalido");
                }
                if (!isBlank(numero) && !empleadosUnicos.add(numero)) {
                    errores.add("Empleado duplicado");
                }

                if (!errores.isEmpty()) {
                    hayErrores = true;
                    row.createCell(5).setCellValue(String.join(" | ", errores));
                    continue;
                }

                Map<String, Object> detalle = new HashMap<>();
                detalle.put("numeroEmpleado", numero);
                detalle.put("monto", BigDecimal.valueOf(importe));
                detalle.put("descripcion", "DISPERSION");
                detalles.add(detalle);
                sumaImportes += importe;
            }

            Double totalCapturado = cellAsDouble(hoja.getRow(2) != null ? hoja.getRow(2).getCell(1) : null, formatter);
            if (totalCapturado != null && Math.abs(totalCapturado - sumaImportes) > 0.01d) {
                hayErrores = true;
                Row totalRow = hoja.getRow(2) != null ? hoja.getRow(2) : hoja.createRow(2);
                totalRow.createCell(5).setCellValue("El TOTAL no coincide con la suma de importes");
            }

            if (detalles.isEmpty()) {
                hayErrores = true;
                Row header = hoja.getRow(3) != null ? hoja.getRow(3) : hoja.createRow(3);
                header.createCell(5).setCellValue("Sin registros validos para procesar");
            }

            if (hayErrores) {
                wb.write(out);
                Map<String, Object> fail = new HashMap<>();
                fail.put("respuesta", false);
                fail.put("object", Base64.getEncoder().encodeToString(out.toByteArray()));
                return fail;
            }

            BigDecimal totalPedido = BigDecimal.valueOf(sumaImportes);
            Long prefacturaId = registrarPrefactura(clienteId, consignatarioId, totalPedido, "DESP");

            Map<String, Object> pedido = new HashMap<>();
            pedido.put("tipoPedido", "DISPERSION");
            pedido.put("descripcion", "Prefactura generada desde procesamiento ");
            pedido.put("montoTotal", totalPedido);
            pedido.put("productoId", 43);
            pedido.put("clienteId", clienteId);
            pedido.put("consignatarioId", consignatarioId);
            pedido.put("idUsuario", idUsuario != null ? idUsuario : 0L);
            pedido.put("prefacturaId", prefacturaId);
            pedido.put("prefactura", Map.of(
                    "prefacturaid", prefacturaId,
                    "clienteid", clienteId,
                    "consignatarioid", consignatarioId,
                    "servicioid", "DESP",
                    "total", totalPedido
            ));
            pedido.put("detalles", detalles);

            Map<String, Object> ok = new HashMap<>();
            ok.put("respuesta", true);
            ok.put("pedido", pedido);
            if (promociones != null) {
                ok.put("promociones", promociones);
            }
            return ok;
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible procesar el archivo de dispersion", ex);
        }
    }

    public byte[] generarPlantillaTarjeta(String data, boolean adicional) {
        Long clienteId = resolveContextLong(data, "clienteId", contextProvider.getClienteId());
        Long consignatarioId = resolveContextLong(data, "consignatarioId", contextProvider.getConsignatarioId());

        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo resolver cliente/consignatario para generar la plantilla");
        }

        String tipoPedido = adicional ? "ADICIONAL" : "TITULAR";
        String titulo = adicional ? "CREAR SOLICITUD DE ASIGNACIÓN ADICIONAL" : "CREAR SOLICITUD DE NUEVA ASIGNACIÓN";

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("CREAR PEDIDO");

            Row row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue(titulo);

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("NOVAHUB GESTIÓN INTERNA / " + LocalDateTime.now());

            Row header = sheet.createRow(2);
            header.createCell(0).setCellValue("Cliente");
            header.createCell(1).setCellValue("Tipo Solicitud");
            header.createCell(2).setCellValue("Numero de empleado");
            header.createCell(3).setCellValue("Apellido Paterno");
            header.createCell(4).setCellValue("Apellido Materno");
            header.createCell(5).setCellValue("Nombre");

            int errorCol;
            if (adicional) {
                errorCol = 6;
            } else {
                header.createCell(6).setCellValue("RFC");
                header.createCell(7).setCellValue("Grupo");
                header.createCell(8).setCellValue("NSS");
                header.createCell(9).setCellValue("CURP");
                header.createCell(10).setCellValue("Telefono");
                header.createCell(11).setCellValue("Correo");
                errorCol = 12;
            }

            for (int i = 0; i < 40; i++) {
                Row row = sheet.createRow(3 + i);
                row.createCell(0).setCellValue(clienteId + "-" + consignatarioId);
                row.createCell(1).setCellValue(tipoPedido);
                if (!adicional) {
                    row.createCell(7).setCellValue("SIN GRUPO");
                }
                row.createCell(errorCol).setCellValue("");
            }

            for (int i = 0; i <= errorCol; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible generar la plantilla de tarjeta", ex);
        }
    }

    public Map<String, Object> procesarTarjetaTitular(String user, String b64, String promociones) {
        return procesarTarjeta(user, b64, promociones, false);
    }

    public Map<String, Object> procesarTarjetaAdicional(String user, String b64, String promociones) {
        return procesarTarjeta(user, b64, promociones, true);
    }

    public Map<String, Object> enriquecerPrefactura(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }

        Map<String, Object> pedido = asMap(payload.get("pedido"));
        if (pedido == null) {
            return payload;
        }

        Map<String, Object> prefacturaActual = asMap(pedido.get("prefactura"));
        Long prefacturaId = asLong(pedido.get("prefacturaId"));
        if (prefacturaId == null && prefacturaActual != null) {
            prefacturaId = asLong(prefacturaActual.get("prefacturaid"));
        }
        if (prefacturaId == null) {
            return payload;
        }

        Long clienteId = asLong(pedido.get("clienteId"));
        if (clienteId == null && prefacturaActual != null) {
            clienteId = asLong(prefacturaActual.get("clienteid"));
        }

        Long consignatarioId = asLong(pedido.get("consignatarioId"));
        if (consignatarioId == null && prefacturaActual != null) {
            consignatarioId = asLong(prefacturaActual.get("consignatarioid"));
        }

        String servicioId = prefacturaActual != null && prefacturaActual.get("servicioid") != null
                ? String.valueOf(prefacturaActual.get("servicioid"))
                : "DESP";

        BigDecimal totalFallback = asBigDecimal(pedido.get("montoTotal"));
        Long productoId = asLong(pedido.get("productoId"));
        List<Map<String, Object>> detallesPedido = asListOfMaps(pedido.get("detalles"));

        Map<String, Object> prefacturaCompleta = construirPrefacturaCompleta(
                prefacturaId,
                clienteId,
                consignatarioId,
                servicioId,
            totalFallback,
            productoId,
            detallesPedido
        );

        pedido.put("prefactura", prefacturaCompleta);
        return payload;
    }

    public Map<String, Object> construirPrefacturaCompleta(
            Long prefacturaId,
            Long clienteId,
            Long consignatarioId,
            String servicioId,
            BigDecimal totalFallback,
            Long productoId,
            List<Map<String, Object>> detallesPedido
    ) {
        Map<String, Object> prefactura = new LinkedHashMap<>();

        if (prefacturaId != null) {
            try {
                List<Map<String, Object>> prefacturas = megadbpedidoJdbc.queryForList(
                        "SELECT " +
                                "prefacturaid AS prefacturaid, " +
                                "clienteid AS clienteid, " +
                                "consignatarioid AS consignatarioid, " +
                                "datosfiscalid AS datosfiscalid, " +
                                "servicioid AS servicioid, " +
                                "total AS total, " +
                                "fechacreacion AS fechacreacion, " +
                                "fechamodificacion AS fechamodificacion, " +
                                "activo AS activo " +
                                "FROM prefactura " +
                                "WHERE prefacturaid = ? " +
                                "ORDER BY prefacturaid DESC " +
                                "LIMIT 1",
                        prefacturaId
                );
                if (!prefacturas.isEmpty()) {
                    prefactura.putAll(prefacturas.get(0));
                }
            } catch (Exception ex) {
                log.warn("No fue posible recuperar prefactura {} completa: {}", prefacturaId, ex.getMessage());
            }
        }

        prefactura.putIfAbsent("prefacturaid", prefacturaId != null ? prefacturaId : 0L);
        prefactura.putIfAbsent("clienteid", clienteId != null ? clienteId : 0L);
        prefactura.putIfAbsent("consignatarioid", consignatarioId != null ? consignatarioId : 0L);
        prefactura.putIfAbsent("servicioid", (servicioId == null || servicioId.isBlank()) ? "DESP" : servicioId);
        prefactura.putIfAbsent("total", totalFallback != null ? totalFallback : BigDecimal.ZERO);

        Long prefacturaIdFinal = asLong(prefactura.get("prefacturaid"));
        List<Map<String, Object>> desgloseList = new ArrayList<>();
        if (prefacturaIdFinal != null && prefacturaIdFinal > 0) {
            try {
                desgloseList = megadbpedidoJdbc.queryForList(
                        "SELECT " +
                                "d.desgloseprefacturaid AS desgloseprefacturaid, " +
                                "d.prefacturaid AS prefacturaid, " +
                                "d.clienteid AS clienteid, " +
                                "d.consignatarioid AS consignatarioid, " +
                                "d.productoid AS productoid, " +
                                "p.nombre AS productonombre, " +
                                "d.valoriva AS valoriva, " +
                                "d.valorbase AS valorbase, " +
                                "d.valorcuotaiva AS valorcuotaiva, " +
                                "d.valorfacial AS valorfacial, " +
                                "d.subtotal AS cobrossubtotal, " +
                                "d.cuotaiva AS cobroscuotaiva, " +
                                "d.total AS cobrostotal, " +
                                "d.activo AS activo " +
                                "FROM desgloseprefactura d " +
                                "LEFT JOIN producto p ON p.productoid = d.productoid " +
                                "WHERE d.prefacturaid = ? AND d.activo = TRUE " +
                                "ORDER BY d.desgloseprefacturaid",
                        prefacturaIdFinal
                );

                for (Map<String, Object> desglose : desgloseList) {
                    Long desgloseId = asLong(desglose.get("desgloseprefacturaid"));
                    List<Map<String, Object>> detalles = new ArrayList<>();
                    if (desgloseId != null) {
                        detalles = megadbpedidoJdbc.queryForList(
                                "SELECT " +
                                        "tipoconceptoid AS tipoconceptoid, " +
                                        "conceptoid AS conceptoid, " +
                                        "nombrecorto AS porconcepto, " +
                                        "preciounitario AS preciounitario, " +
                                        "cantidad AS cantidad, " +
                                        "subtotal AS subtotal " +
                                        "FROM detalleprefactura " +
                                        "WHERE desgloseprefacturaid = ? " +
                                        "ORDER BY detalleprefacturaid",
                                desgloseId
                        );
                    }
                    desglose.put("ltsDetallePrectafura", detalles);
                }
            } catch (Exception ex) {
                log.warn("No fue posible recuperar desglose/detalle de prefactura {}: {}", prefacturaIdFinal, ex.getMessage());
            }
        }

        if (desgloseList.isEmpty()) {
            desgloseList = construirDesgloseFallback(
                    prefacturaIdFinal,
                    clienteId,
                    consignatarioId,
                    productoId,
                    totalFallback,
                    detallesPedido
            );
        }
        prefactura.put("ltsDesglosePrefactura", desgloseList);

        Map<String, Object> datosFiscales = new LinkedHashMap<>();
        Long datosFiscalId = asLong(prefactura.get("datosfiscalid"));
        if (datosFiscalId != null && datosFiscalId > 0) {
            try {
                List<Map<String, Object>> fiscales = megadbpedidoJdbc.queryForList(
                        "SELECT " +
                                "df.razonsocial AS razonsocial, " +
                                "df.rfc AS rfc, " +
                                "d.calle AS calle, " +
                                "d.entrecalle1 AS entrecalle1, " +
                                "d.entrecalle2 AS entrecalle2, " +
                                "d.colonia AS colonia, " +
                                "d.numerointerior AS numerointerior, " +
                                "d.numeroexterior AS numeroexterior, " +
                                "d.codigopostal AS codigopostal, " +
                                "d.pais AS pais, " +
                                "d.ciudad AS ciudad " +
                                "FROM datosfiscal df " +
                                "JOIN domicilio d USING(domicilioid) " +
                                "WHERE df.datosfiscalid = ?",
                        datosFiscalId
                );
                if (!fiscales.isEmpty()) {
                    datosFiscales.putAll(fiscales.get(0));
                }
            } catch (Exception ex) {
                log.warn("No fue posible recuperar datos fiscales {}: {}", datosFiscalId, ex.getMessage());
            }
        }

        datosFiscales.putIfAbsent("razonsocial", "-");
        datosFiscales.putIfAbsent("rfc", "-");
        datosFiscales.putIfAbsent("calle", "-");
        datosFiscales.putIfAbsent("numerointerior", "-");
        datosFiscales.putIfAbsent("numeroexterior", "-");
        datosFiscales.putIfAbsent("colonia", "-");
        datosFiscales.putIfAbsent("codigopostal", "-");
        datosFiscales.putIfAbsent("ciudad", "-");
        prefactura.put("datosFiscales", datosFiscales);

        return prefactura;
    }

    private Map<String, Object> procesarTarjeta(String user, String b64, String promociones, boolean adicional) {
        Long clienteId = resolveContextLong(user, "clienteId", contextProvider.getClienteId());
        Long consignatarioId = resolveContextLong(user, "consignatarioId", contextProvider.getConsignatarioId());
        Long idUsuario = resolveContextLong(user, "idUsuario", contextProvider.getIdUsuario());

        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo resolver cliente/consignatario de la sesion");
        }
        if (b64 == null || b64.isBlank()) {
            throw new IllegalArgumentException("No se recibio archivo para procesar");
        }

        String normalizado = b64.contains(",") ? b64.substring(b64.indexOf(',') + 1) : b64;
        DataFormatter formatter = new DataFormatter();
        String tipoEsperado = adicional ? "ADICIONAL" : "TITULAR";
        int errorCol = adicional ? 6 : 12;

        try (ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(normalizado));
             XSSFWorkbook wb = new XSSFWorkbook(in);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet hoja = wb.getSheetAt(0);
            if (hoja == null) {
                throw new IllegalArgumentException("El archivo no contiene hoja valida");
            }

            Set<String> empleadosUnicos = new LinkedHashSet<>();
            List<Map<String, Object>> detalles = new ArrayList<>();
            boolean hayErrores = false;

            for (int idx = 3; idx <= hoja.getLastRowNum(); idx++) {
                Row row = hoja.getRow(idx);
                if (row == null) {
                    continue;
                }

                String clienteCell = cellAsString(row.getCell(0), formatter);
                String tipoPedido = cellAsString(row.getCell(1), formatter).toUpperCase();
                String numeroEmpleado = cellAsString(row.getCell(2), formatter);
                String apellidoPaterno = cellAsString(row.getCell(3), formatter);
                String apellidoMaterno = cellAsString(row.getCell(4), formatter);
                String nombre = cellAsString(row.getCell(5), formatter);

                if (isBlank(numeroEmpleado) && isBlank(nombre) && isBlank(apellidoPaterno)) {
                    continue;
                }

                List<String> errores = new ArrayList<>();
                if (isBlank(clienteCell) || !clienteCell.equals(clienteId + "-" + consignatarioId)) {
                    errores.add("Cliente/consignatario invalido");
                }
                if (!tipoEsperado.equals(tipoPedido)) {
                    errores.add("Tipo pedido invalido");
                }
                if (isBlank(numeroEmpleado)) {
                    errores.add("Numero de empleado requerido");
                }
                if (isBlank(apellidoPaterno) || isBlank(nombre)) {
                    errores.add("Nombre incompleto");
                }
                if (!isBlank(numeroEmpleado) && !empleadosUnicos.add(numeroEmpleado)) {
                    errores.add("Empleado duplicado");
                }

                if (!errores.isEmpty()) {
                    hayErrores = true;
                    row.createCell(errorCol).setCellValue(String.join(" | ", errores));
                    continue;
                }

                StringBuilder nombreCompleto = new StringBuilder();
                nombreCompleto.append(apellidoPaterno.trim()).append(' ');
                if (!isBlank(apellidoMaterno)) {
                    nombreCompleto.append(apellidoMaterno.trim()).append(' ');
                }
                nombreCompleto.append(nombre.trim());

                Map<String, Object> detalle = new HashMap<>();
                detalle.put("numeroEmpleado", numeroEmpleado);
                detalle.put("nombreEmpleado", nombreCompleto.toString().trim());
                detalle.put("descripcion", tipoEsperado);
                detalle.put("monto", BigDecimal.ZERO);
                detalles.add(detalle);
            }

            if (detalles.isEmpty()) {
                hayErrores = true;
                Row header = hoja.getRow(2) != null ? hoja.getRow(2) : hoja.createRow(2);
                header.createCell(errorCol).setCellValue("Sin registros validos para procesar");
            }

            if (hayErrores) {
                wb.write(out);
                Map<String, Object> fail = new HashMap<>();
                fail.put("respuesta", false);
                fail.put("object", Base64.getEncoder().encodeToString(out.toByteArray()));
                return fail;
            }

            BigDecimal totalPedido = BigDecimal.ZERO;
            Long prefacturaId = registrarPrefactura(clienteId, consignatarioId, totalPedido, "DESP");

            Map<String, Object> pedido = new HashMap<>();
            pedido.put("tipoPedido", adicional ? "ADICIONAL" : "TARJETA");
            pedido.put("descripcion", adicional
                    ? "Prefactura generada desde procesamiento de asignaciÓn adicional"
                    : "Prefactura generada desde procesamiento de nueva asignaciÓn");
            pedido.put("montoTotal", totalPedido);
            pedido.put("productoId", adicional ? 46 : 45);
            pedido.put("clienteId", clienteId);
            pedido.put("consignatarioId", consignatarioId);
            pedido.put("idUsuario", idUsuario != null ? idUsuario : 0L);
            pedido.put("prefacturaId", prefacturaId);
            pedido.put("prefactura", Map.of(
                    "prefacturaid", prefacturaId,
                    "clienteid", clienteId,
                    "consignatarioid", consignatarioId,
                    "servicioid", "DESP",
                    "total", totalPedido
            ));
            pedido.put("detalles", detalles);

            Map<String, Object> ok = new HashMap<>();
            ok.put("respuesta", true);
            ok.put("pedido", pedido);
            if (promociones != null) {
                ok.put("promociones", promociones);
            }
            return ok;
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible procesar el archivo de credenciales", ex);
        }
    }

    private String cellAsString(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return new BigDecimal(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asListOfMaps(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                result.add((Map<String, Object>) map);
            }
        }
        return result;
    }

    private List<Map<String, Object>> construirDesgloseFallback(
            Long prefacturaId,
            Long clienteId,
            Long consignatarioId,
            Long productoId,
            BigDecimal totalFallback,
            List<Map<String, Object>> detallesPedido
    ) {
        BigDecimal totalSeguro = totalFallback != null ? totalFallback : BigDecimal.ZERO;

        Map<String, Object> desglose = new LinkedHashMap<>();
        desglose.put("desgloseprefacturaid", 0L);
        desglose.put("prefacturaid", prefacturaId != null ? prefacturaId : 0L);
        desglose.put("clienteid", clienteId != null ? clienteId : 0L);
        desglose.put("consignatarioid", consignatarioId != null ? consignatarioId : 0L);
        desglose.put("productoid", productoId != null ? productoId : 0L);
        desglose.put("productonombre", resolverNombreProducto(productoId));
        desglose.put("valoriva", BigDecimal.ZERO);
        desglose.put("valorbase", totalSeguro);
        desglose.put("valorcuotaiva", BigDecimal.ZERO);
        desglose.put("valorfacial", totalSeguro);
        desglose.put("cobrossubtotal", totalSeguro);
        desglose.put("cobroscuotaiva", BigDecimal.ZERO);
        desglose.put("cobrostotal", totalSeguro);
        desglose.put("activo", true);

        List<Map<String, Object>> detalleRows = new ArrayList<>();
        if (detallesPedido != null && !detallesPedido.isEmpty()) {
            for (Map<String, Object> item : detallesPedido) {
                Map<String, Object> detalle = new LinkedHashMap<>();
                detalle.put("tipoconceptoid", "LEG");
                detalle.put("conceptoid", 0);
                detalle.put("porconcepto", item.getOrDefault("descripcion", "Detalle"));
                BigDecimal monto = asBigDecimal(item.get("monto"));
                detalle.put("preciounitario", monto != null ? monto : BigDecimal.ZERO);
                detalle.put("cantidad", 1);
                detalle.put("subtotal", monto != null ? monto : BigDecimal.ZERO);
                detalleRows.add(detalle);
            }
        }

        if (detalleRows.isEmpty()) {
            Map<String, Object> detalle = new LinkedHashMap<>();
            detalle.put("tipoconceptoid", "LEG");
            detalle.put("conceptoid", 0);
            detalle.put("porconcepto", "Prefactura generada");
            detalle.put("preciounitario", totalSeguro);
            detalle.put("cantidad", 1);
            detalle.put("subtotal", totalSeguro);
            detalleRows.add(detalle);
        }

        desglose.put("ltsDetallePrectafura", detalleRows);
        return List.of(desglose);
    }

    private String resolverNombreProducto(Long productoId) {
        if (productoId == null) {
            return "PRODUCTO";
        }
        try {
            String nombre = megadbpedidoJdbc.query(
                    "SELECT nombre FROM producto WHERE productoid = ? LIMIT 1",
                    rs -> rs.next() ? rs.getString(1) : null,
                    productoId
            );
            return (nombre == null || nombre.isBlank()) ? "PRODUCTO" : nombre;
        } catch (Exception ignored) {
            return "PRODUCTO";
        }
    }

    private Double cellAsDouble(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        String raw = formatter.formatCellValue(cell).trim().replace(",", "");
        if (raw.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long mapearProductoPorTipoPedido(String tipoPedido) {
        if (tipoPedido == null) {
            return null;
        }
        return switch (tipoPedido.toUpperCase()) {
            case "TARJETA" -> 45L;
            case "ADICIONAL" -> 46L;
            case "STOCK" -> 47L;
            default -> 43L;
        };
    }

    private Long resolveContextLong(String payload, String field, Long fallback) {
        if (payload != null && !payload.isBlank()) {
            try {
                Map<?, ?> map = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload, Map.class);
                Object value = map.get(field);
                if (value instanceof Number number) {
                    return number.longValue();
                }
                if (value instanceof String text && !text.isBlank()) {
                    return Long.parseLong(text);
                }
            } catch (Exception ignored) {
                // fallback below
            }
        }
        return fallback;
    }

    private String resolveContextString(String payload, String field, String fallback) {
        if (payload != null && !payload.isBlank()) {
            try {
                Map<?, ?> map = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload, Map.class);
                Object value = map.get(field);
                if (value != null) {
                    String text = String.valueOf(value).trim();
                    if (!text.isBlank()) {
                        return text;
                    }
                }
            } catch (Exception ignored) {
                // fallback below
            }
        }
        return fallback;
    }
}

