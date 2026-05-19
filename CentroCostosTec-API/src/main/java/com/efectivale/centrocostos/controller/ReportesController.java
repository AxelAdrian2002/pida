package com.efectivale.centrocostos.controller;

import com.efectivale.centrocostos.dto.ApiResponse;
import com.efectivale.centrocostos.security.ContextProvider;
import com.efectivale.centrocostos.service.SolicitudAuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReportesController {

    private final SolicitudAuditoriaService auditoriaService;
    private final ContextProvider contextProvider;

    /**
     * Obtiene historial de cambios de una solicitud específica
     */
    @GetMapping("/solicitudes/{id}/historial")
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA','EMPLEADO')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> obtenerHistorialSolicitud(
            @PathVariable Long id) {
        List<Map<String, Object>> historial = auditoriaService.obtenerHistorialSolicitud(id);
        return ResponseEntity.ok(ApiResponse.exito(historial));
    }

    /**
     * Reporte de auditoría por cliente y rango de fechas
     */
    @GetMapping("/auditoria")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerAuditoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();
        
        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de cliente/consignatario");
        }

        LocalDateTime inicio = LocalDateTime.of(fechaInicio, LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.of(fechaFin, LocalTime.MAX);

        List<Map<String, Object>> registros = auditoriaService.obtenerAuditoriaPorCliente(
            clienteId, consignatarioId, inicio, fin
        );

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("cliente_id", clienteId);
        reporte.put("consignatario_id", consignatarioId);
        reporte.put("fecha_inicio", fechaInicio);
        reporte.put("fecha_fin", fechaFin);
        reporte.put("total_cambios", registros.size());
        reporte.put("registros", registros);

        // Agrupar por acción
        Map<String, Long> porAccion = registros.stream()
            .collect(Collectors.groupingBy(
                r -> (String) r.get("accion"),
                Collectors.counting()
            ));
        reporte.put("resumen_por_accion", porAccion);

        // Agrupar por tipo de solicitud
        Map<String, Long> porTipo = registros.stream()
            .collect(Collectors.groupingBy(
                r -> (String) r.get("tipo_solicitud"),
                Collectors.counting()
            ));
        reporte.put("resumen_por_tipo", porTipo);

        return ResponseEntity.ok(ApiResponse.exito(reporte));
    }

    /**
     * Resumen rápido de solicitudes por cliente
     */
    @GetMapping("/solicitudes/resumen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerResumenSolicitudes() {
        
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();
        
        if (clienteId == null || consignatarioId == null) {
            throw new IllegalStateException("No se pudo obtener el contexto de cliente/consignatario");
        }

        // Obtener auditoría del último mes
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        LocalDateTime ahora = LocalDateTime.now();

        List<Map<String, Object>> registros = auditoriaService.obtenerAuditoriaPorCliente(
            clienteId, consignatarioId, hace30Dias, ahora
        );

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("cliente_id", clienteId);
        resumen.put("consignatario_id", consignatarioId);
        resumen.put("periodo", "Últimos 30 días");
        resumen.put("total_cambios", registros.size());

        // Contar por acción
        long creaciones = registros.stream().filter(r -> "CREAR".equals(r.get("accion"))).count();
        long autorizaciones = registros.stream().filter(r -> "AUTORIZAR".equals(r.get("accion"))).count();
        long rechazos = registros.stream().filter(r -> "RECHAZAR".equals(r.get("accion"))).count();
        long cancelaciones = registros.stream().filter(r -> "CANCELAR".equals(r.get("accion"))).count();

        resumen.put("creaciones", creaciones);
        resumen.put("autorizaciones", autorizaciones);
        resumen.put("rechazos", rechazos);
        resumen.put("cancelaciones", cancelaciones);

        // Contar por tipo de solicitud
        Map<String, Long> porTipo = registros.stream()
            .collect(Collectors.groupingBy(
                r -> (String) r.getOrDefault("tipo_solicitud", "DESCONOCIDO"),
                Collectors.counting()
            ));
        resumen.put("por_tipo_solicitud", porTipo);

        return ResponseEntity.ok(ApiResponse.exito(resumen));
    }

    /**
     * Filtro de auditoría con criterios específicos (para admins avanzados)
     */
    @PostMapping("/auditoria/filtro")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> filtrarAuditoria(
            @RequestBody Map<String, Object> filtros) {
        
        Long clienteId = contextProvider.getClienteId();
        Long consignatarioId = contextProvider.getConsignatarioId();

        @SuppressWarnings("unchecked")
        LocalDate fechaInicio = (LocalDate) filtros.get("fechaInicio");
        @SuppressWarnings("unchecked")
        LocalDate fechaFin = (LocalDate) filtros.get("fechaFin");
        @SuppressWarnings("unchecked")
        String accion = (String) filtros.get("accion");
        @SuppressWarnings("unchecked")
        String tipoSolicitud = (String) filtros.get("tipoSolicitud");

        LocalDateTime inicio = LocalDateTime.of(fechaInicio != null ? fechaInicio : LocalDate.now().minusDays(30), LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.of(fechaFin != null ? fechaFin : LocalDate.now(), LocalTime.MAX);

        List<Map<String, Object>> registros = auditoriaService.obtenerAuditoriaPorCliente(
            clienteId, consignatarioId, inicio, fin
        );

        // Aplicar filtros
        if (accion != null && !accion.isBlank()) {
            registros = registros.stream()
                .filter(r -> accion.equalsIgnoreCase((String) r.get("accion")))
                .collect(Collectors.toList());
        }

        if (tipoSolicitud != null && !tipoSolicitud.isBlank()) {
            registros = registros.stream()
                .filter(r -> tipoSolicitud.equalsIgnoreCase((String) r.get("tipo_solicitud")))
                .collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.exito(registros));
    }
}
