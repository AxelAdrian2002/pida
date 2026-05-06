package com.efectivale.centrocostos.controller;

import com.efectivale.centrocostos.dto.ApiResponse;

import com.efectivale.centrocostos.dto.SolicitudDto;
import com.efectivale.centrocostos.entity.Solicitud;
import com.efectivale.centrocostos.security.ContextProvider;
import com.efectivale.centrocostos.service.SolicitudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@RestController
@RequestMapping
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final ObjectMapper objectMapper;
    private final ContextProvider contextProvider;

    public SolicitudController(SolicitudService solicitudService, ObjectMapper objectMapper, ContextProvider contextProvider) {
        this.solicitudService = solicitudService;
        this.objectMapper = objectMapper;
        this.contextProvider = contextProvider;
    }

        @GetMapping("/solicitudes")
    public ResponseEntity<ApiResponse<Page<Solicitud>>> listar(
            @RequestParam(required = false) String estado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.exito(solicitudService.consultarSolicitudes(estado, pageable)));
    }

        @GetMapping("/solicitudes/{id}")
    public ResponseEntity<ApiResponse<Solicitud>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.exito(solicitudService.obtenerSolicitud(id)));
    }

        @PostMapping(value = {
            "/solicitudes/apoyo-economico",
            "/Administracion_Pedidos/crear_pedido_dispersion/v1/crear_pedido_dispersion/guardarPedido"
        }, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Solicitud>> crearDispersion(@Valid @RequestBody SolicitudDto dto) {
        dto.setTipoSolicitud("DISPERSION");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.creado(solicitudService.crearSolicitudDispersion(dto)));
    }

        @PostMapping(
            value = {
                "/solicitudes/apoyo-economico",
                "/Administracion_Pedidos/crear_pedido_dispersion/v1/crear_pedido_dispersion/guardarPedido"
            },
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
        )
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Solicitud>> crearDispersion(
            @RequestParam String data,
            @RequestParam Long userId) throws Exception {
        SolicitudDto dto = objectMapper.readValue(data, SolicitudDto.class);
        dto.setIdUsuario(userId);
        dto.setTipoSolicitud("DISPERSION");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.creado(solicitudService.crearSolicitudDispersion(dto)));
    }

        @PostMapping(value = {
            "/solicitudes/reposicion",
            "/Administracion_Pedidos/crear_pedido_stock/v1/crear_pedido_stock/guardarPedido"
        }, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Solicitud>> crearStock(@Valid @RequestBody SolicitudDto dto) {
        dto.setTipoSolicitud("STOCK");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.creado(solicitudService.crearSolicitudStock(dto)));
    }

        @PostMapping(
            value = {
                "/solicitudes/reposicion",
                "/Administracion_Pedidos/crear_pedido_stock/v1/crear_pedido_stock/guardarPedido"
            },
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
        )
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Solicitud>> crearStock(
            @RequestParam String data,
            @RequestParam Long userId) throws Exception {
        SolicitudDto dto = objectMapper.readValue(data, SolicitudDto.class);
        dto.setIdUsuario(userId);
        dto.setTipoSolicitud("STOCK");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.creado(solicitudService.crearSolicitudStock(dto)));
    }

        @PostMapping(value = {
            "/solicitudes/nueva-asignacion",
            "/Administracion_Pedidos/crear_pedido_tarjeta/v1/crear_pedido_tarjeta/guardarPedido"
        }, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Solicitud>> crearTarjeta(@Valid @RequestBody SolicitudDto dto) {
        dto.setTipoSolicitud("TARJETA");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.creado(solicitudService.crearSolicitudAsignacion(dto)));
    }

        @PostMapping(value = {
            "/solicitudes/asignacion-adicional",
            "/Administracion_Pedidos/crear_pedido_adicional/v1/crear_pedido_adicional/guardarPedido"
        }, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Solicitud>> crearAdicional(@Valid @RequestBody SolicitudDto dto) {
        dto.setTipoSolicitud("ADICIONAL");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.creado(solicitudService.crearSolicitudAdicional(dto)));
    }

        @PostMapping(
            value = {
                "/solicitudes/nueva-asignacion",
                "/Administracion_Pedidos/crear_pedido_tarjeta/v1/crear_pedido_tarjeta/guardarPedido"
            },
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
        )
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Solicitud>> crearTarjeta(
            @RequestParam String data,
            @RequestParam Long userId) throws Exception {
        SolicitudDto dto = objectMapper.readValue(data, SolicitudDto.class);
        dto.setIdUsuario(userId);
        dto.setTipoSolicitud("TARJETA");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.creado(solicitudService.crearSolicitudAsignacion(dto)));
    }

        @PostMapping(
            value = {
                "/solicitudes/asignacion-adicional",
                "/Administracion_Pedidos/crear_pedido_adicional/v1/crear_pedido_adicional/guardarPedido"
            },
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
        )
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Solicitud>> crearAdicional(
            @RequestParam String data,
            @RequestParam Long userId) throws Exception {
        SolicitudDto dto = objectMapper.readValue(data, SolicitudDto.class);
        dto.setIdUsuario(userId);
        dto.setTipoSolicitud("ADICIONAL");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.creado(solicitudService.crearSolicitudAdicional(dto)));
    }

        @GetMapping({
            "/solicitudes/apoyo-economico/plantilla",
            "/solicitudes/nueva-asignacion/plantilla",
            "/solicitudes/asignacion-adicional/plantilla",
            "/Administracion_Pedidos/crear_pedido_dispersion/v1/crear_pedido_dispersion/getPlantilla",
            "/Administracion_Pedidos/crear_pedido_tarjeta/v1/crear_pedido_tarjeta/getPlantilla",
            "/Administracion_Pedidos/crear_pedido_adicional/v1/crear_pedido_adicional/getPlantilla"
        })
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<byte[]> getPlantilla(
            @RequestParam(required = false) String data,
            HttpServletRequest request) throws IOException {
        String tipo = inferirTipoPlantilla(request.getRequestURI());
        byte[] archivo;
        if ("DISPERSION".equals(tipo)) {
            archivo = solicitudService.generarPlantillaDispersion(data);
        } else if ("TARJETA".equals(tipo)) {
            archivo = solicitudService.generarPlantillaTarjeta(data, false);
        } else if ("ADICIONAL".equals(tipo)) {
            archivo = solicitudService.generarPlantillaTarjeta(data, true);
        } else {
            archivo = generarPlantillaXlsx(tipo, data);
        }
        String nombre = "plantilla_" + tipo.toLowerCase() + ".xlsx";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(archivo);
    }

        @PostMapping({
            "/solicitudes/apoyo-economico/procesar",
            "/solicitudes/nueva-asignacion/procesar",
            "/solicitudes/asignacion-adicional/procesar",
            "/Administracion_Pedidos/crear_pedido_dispersion/v1/crear_pedido_dispersion/procesar",
            "/Administracion_Pedidos/crear_pedido_tarjeta/v1/crear_pedido_tarjeta/procesar",
            "/Administracion_Pedidos/crear_pedido_adicional/v1/crear_pedido_adicional/procesar"
        })
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> procesar(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String b64,
            @RequestParam(required = false) String promociones,
            HttpServletRequest request) {
        String tipoPedido = inferirTipoPedido(request.getRequestURI());

        if ("DISPERSION".equals(tipoPedido)) {
            Map<String, Object> response = solicitudService.procesarDispersion(user, b64, promociones);
                solicitudService.enriquecerPrefactura(response);
            return ResponseEntity.ok(ApiResponse.exito(response));
        }

        if ("TARJETA".equals(tipoPedido)) {
            Map<String, Object> response = solicitudService.procesarTarjetaTitular(user, b64, promociones);
                solicitudService.enriquecerPrefactura(response);
            return ResponseEntity.ok(ApiResponse.exito(response));
        }

        if ("ADICIONAL".equals(tipoPedido)) {
            Map<String, Object> response = solicitudService.procesarTarjetaAdicional(user, b64, promociones);
                solicitudService.enriquecerPrefactura(response);
            return ResponseEntity.ok(ApiResponse.exito(response));
        }

        Map<String, Object> pedido = new HashMap<>();
        Long clienteId = parseLongContexto(user, "clienteId");
        Long consignatarioId = parseLongContexto(user, "consignatarioId");

        Long clienteFinal = clienteId != null ? clienteId : 0L;
        Long consignatarioFinal = consignatarioId != null ? consignatarioId : 0L;
            Long prefacturaId = solicitudService.registrarPrefactura(clienteFinal, consignatarioFinal, BigDecimal.ZERO, "DESP");

        pedido.put("tipoPedido", tipoPedido);
        pedido.put("descripcion", "Prefactura generada desde procesamiento ");
        pedido.put("montoTotal", BigDecimal.ZERO);
        pedido.put("productoId", productoPorTipo(tipoPedido));
        pedido.put("clienteId", clienteFinal);
        pedido.put("consignatarioId", consignatarioFinal);
        pedido.put("prefacturaId", prefacturaId);
        pedido.put("prefactura", Map.of(
                "prefacturaid", prefacturaId,
                "clienteid", clienteFinal,
                "consignatarioid", consignatarioFinal,
                "servicioid", "DESP",
                "total", BigDecimal.ZERO
        ));
        pedido.put("detalles", List.of());

        Map<String, Object> response = new HashMap<>();
        response.put("respuesta", true);
        response.put("pedido", pedido);
        response.put("user", user);
        response.put("promociones", promociones);
        response.put("archivo", b64 != null ? "recibido" : "no-recebido");
            solicitudService.enriquecerPrefactura(response);
        return ResponseEntity.ok(ApiResponse.exito(response));
    }

        @PostMapping({"/solicitudes/reposicion/procesar", "/Administracion_Pedidos/crear_pedido_stock/v1/crear_pedido_stock/procesar"})
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> procesarStock(
            @RequestParam(required = false) String data) {
        Long clienteId = parseLongContexto(data, "clienteId");
        Long consignatarioId = parseLongContexto(data, "consignatarioId");
        Long clienteFinal = clienteId != null ? clienteId : 0L;
        Long consignatarioFinal = consignatarioId != null ? consignatarioId : 0L;

        Long prefacturaId = solicitudService.registrarPrefactura(clienteFinal, consignatarioFinal, BigDecimal.ZERO, "DESP");

        Map<String, Object> pedido = new HashMap<>();
        pedido.put("tipoPedido", "STOCK");
        pedido.put("descripcion", "Prefactura stock generada");
        pedido.put("montoTotal", BigDecimal.ZERO);
        pedido.put("productoId", 47);
        pedido.put("clienteId", clienteFinal);
        pedido.put("consignatarioId", consignatarioFinal);
        pedido.put("prefacturaId", prefacturaId);
        pedido.put("prefactura", Map.of(
                "prefacturaid", prefacturaId,
                "clienteid", clienteFinal,
                "consignatarioid", consignatarioFinal,
                "servicioid", "DESP",
                "total", BigDecimal.ZERO
        ));
        pedido.put("detalles", List.of());

        Map<String, Object> response = new HashMap<>();
        response.put("respuesta", true);
        response.put("pedido", pedido);
        response.put("data", data);
        solicitudService.enriquecerPrefactura(response);
        return ResponseEntity.ok(ApiResponse.exito(response));
    }

    @GetMapping({"/solicitudes/resumen/getVista", "/Administracion_Pedidos/prefactura/v1/prefactura/getVista"})
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVistaPrefacturaGuardado(
            @RequestParam(required = false) Long pedidoId,
            @RequestParam(required = false) Long prefacturaId) {
        Map<String, Object> vista = solicitudService.recuperarVistaPrefacturaGuardado(pedidoId, prefacturaId);
        return ResponseEntity.ok(ApiResponse.exito(vista));
    }

    private String inferirTipoPedido(String uri) {
        if (uri == null) {
            return "DISPERSION";
        }
        if (uri.contains("apoyo-economico")) {
            return "DISPERSION";
        }
        if (uri.contains("nueva-asignacion")) {
            return "TARJETA";
        }
        if (uri.contains("asignacion-adicional")) {
            return "ADICIONAL";
        }
        if (uri.contains("crear_pedido_tarjeta")) {
            return "TARJETA";
        }
        if (uri.contains("crear_pedido_adicional")) {
            return "ADICIONAL";
        }
        return "DISPERSION";
    }

    private int productoPorTipo(String tipoPedido) {
        return switch (tipoPedido) {
            case "TARJETA" -> 45;
            case "ADICIONAL" -> 46;
            case "STOCK" -> 47;
            default -> 43;
        };
    }

    private Long parseLongContexto(String user, String field) {
        if (user == null || user.isBlank()) {
            return null;
        }
        try {
            Map<?, ?> contexto = objectMapper.readValue(user, Map.class);
            Object valor = contexto.get(field);
            if (valor instanceof Number n) {
                return n.longValue();
            }
            if (valor instanceof String s && !s.isBlank()) {
                return Long.parseLong(s);
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private String inferirTipoPlantilla(String uri) {
        if (uri == null) {
            return "DISPERSION";
        }
        if (uri.contains("apoyo-economico")) {
            return "DISPERSION";
        }
        if (uri.contains("nueva-asignacion")) {
            return "TARJETA";
        }
        if (uri.contains("asignacion-adicional")) {
            return "ADICIONAL";
        }
        if (uri.contains("crear_pedido_tarjeta")) {
            return "TARJETA";
        }
        if (uri.contains("crear_pedido_adicional")) {
            return "ADICIONAL";
        }
        return "DISPERSION";
    }

    private byte[] generarPlantillaXlsx(String tipo, String data) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("PLANTILLA_" + tipo);

            XSSFRow header = sheet.createRow(0);
            header.createCell(0).setCellValue("numeroEmpleado");
            if ("DISPERSION".equals(tipo)) {
                header.createCell(1).setCellValue("importe");
            } else {
                header.createCell(1).setCellValue("monto");
            }
            header.createCell(2).setCellValue("descripcion");

            XSSFRow ejemplo = sheet.createRow(1);
            ejemplo.createCell(0).setCellValue("000001");
            ejemplo.createCell(1).setCellValue(100.00);
            ejemplo.createCell(2).setCellValue("Ejemplo " + tipo);

            if (data != null && !data.isBlank()) {
                XSSFRow meta = sheet.createRow(3);
                meta.createCell(0).setCellValue("contexto");
                meta.createCell(1).setCellValue(data);
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            wb.write(out);
            return out.toByteArray();
        }
    }

        @GetMapping({"/solicitudes/aprobacion/catalogos", "/Administracion_Pedidos/autorizar_pedido/v1/autorizar_pedido/getFillSelect/"})
    @PreAuthorize("hasAnyRole('ADMIN','AUTORIZADOR')")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getFillSelect() {
        String centroSesion = null;
        List<Map<String, String>> centros = new java.util.ArrayList<>();
        centros.add(Map.of("id", "ALL", "nombre", "Todos"));
        if (centroSesion != null && !centroSesion.isBlank()) {
            centros.add(Map.of("id", centroSesion, "nombre", centroSesion));
        }
        return ResponseEntity.ok(ApiResponse.exito(centros));
    }

        @GetMapping({"/solicitudes/aprobacion/listado", "/Administracion_Pedidos/autorizar_pedido/v1/autorizar_pedido/getDataTable"})
    @PreAuthorize("hasAnyRole('ADMIN','AUTORIZADOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDataTable(
            @RequestParam(required = false) String data) {
        Map<String, Object> request = new HashMap<>();
        try {
            if (data != null && !data.isBlank()) {
                request = objectMapper.readValue(data, Map.class);
            }
        } catch (Exception ignored) {
            request = new HashMap<>();
        }

        int start = parseInt(request.get("start"), 0);
        int length = parseInt(request.get("length"), 10);
        int draw = parseInt(request.get("draw"), 1);
        if (length <= 0) {
            length = 10;
        }
        int pageNumber = Math.max(0, start / length);

        Page<Solicitud> page = solicitudService.consultarSolicitudes("PENDIENTE", PageRequest.of(pageNumber, length));
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();
        Map<String, BigDecimal> saldos = solicitudService.obtenerSaldoMonedero(clienteId, consignatarioId);
        List<Map<String, Object>> rows = page.getContent().stream().map(p -> Map.<String, Object>of(
                "pedidoid", p.getIdSolicitud(),
            "cliente", p.getClienteId() + " - " + p.getConsignatarioId(),
                "clienteid", p.getClienteId(),
                "consignatarioid", p.getConsignatarioId(),
            "fechacreacion", p.getFechaCreacion() != null ? p.getFechaCreacion().toString() : "",
                "monto", p.getMontoTotal() != null ? p.getMontoTotal() : BigDecimal.ZERO,
            "total", p.getMontoTotal() != null ? p.getMontoTotal() : BigDecimal.ZERO,
            "descripcion", p.getTipoSolicitud() != null ? p.getTipoSolicitud() : "SOLICITUD",
                "concepto", p.getDescripcion() != null ? p.getDescripcion() : "Aprobacion de solicitud"
        )).toList();

        Map<String, Object> dt = new HashMap<>();
        dt.put("draw", draw);
        dt.put("recordsTotal", page.getTotalElements());
        dt.put("recordsFiltered", page.getTotalElements());
        dt.put("data", rows);
        dt.put("monederoSaldo", saldos.getOrDefault("monederoSaldo", BigDecimal.ZERO));
        dt.put("creditoSaldo", saldos.getOrDefault("creditoSaldo", BigDecimal.ZERO));
        dt.put("start", start);
        dt.put("length", length);
        return ResponseEntity.ok(ApiResponse.exito(dt));
    }

        @PutMapping({"/solicitudes/{id}/aprobar", "/pedidos/{id}/autorizar"})
    @PreAuthorize("hasAnyRole('ADMIN','AUTORIZADOR')")
        public ResponseEntity<ApiResponse<Solicitud>> autorizar(
            @PathVariable Long id,
            @RequestParam Long idUsuarioAutoriza,
            @RequestParam(required = false) String observaciones) {
        return ResponseEntity.ok(ApiResponse.exito("Solicitud aprobada exitosamente",
                solicitudService.autorizarSolicitud(id, idUsuarioAutoriza, observaciones)));
    }

        @PostMapping({"/solicitudes/aprobacion", "/Administracion_Pedidos/autorizar_pedido/v1/autorizar_pedido/proccessOrder"})
        @PreAuthorize("hasAnyRole('ADMIN','AUTORIZADOR')")
        public ResponseEntity<ApiResponse<Solicitud>> autorizarCompat(
            @RequestParam Long idPedido,
                @RequestParam(required = false) Long idUsuarioAutoriza,
            @RequestParam(required = false) String observaciones) {
            Long usuarioAutoriza = (idUsuarioAutoriza == null || idUsuarioAutoriza <= 0)
                    ? contextProvider.getIdUsuario()
                    : idUsuarioAutoriza;
            if (usuarioAutoriza == null || usuarioAutoriza <= 0) {
                throw new IllegalArgumentException("No se pudo resolver idUsuarioAutoriza para aprobar la solicitud");
            }
            return ResponseEntity.ok(ApiResponse.exito("Solicitud aprobada exitosamente",
                solicitudService.autorizarSolicitud(idPedido, usuarioAutoriza, observaciones)));
        }

    private int parseInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}

