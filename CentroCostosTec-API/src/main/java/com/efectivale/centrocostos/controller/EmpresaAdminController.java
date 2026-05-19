package com.efectivale.centrocostos.controller;

import com.efectivale.centrocostos.dto.ApiResponse;
import com.efectivale.centrocostos.service.EmpresaAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/empresas/v1")
@RequiredArgsConstructor
public class EmpresaAdminController {

    private final EmpresaAdminService empresaAdminService;

    @GetMapping("/plantilla-empleados")
    @PreAuthorize("@perm.has('EMPLEADOS_IMPORTAR')")
    public ResponseEntity<byte[]> descargarPlantilla() {
        String csv = empresaAdminService.generarPlantillaCsv();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_empleados.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping(path = "/cargar-empleados", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@perm.has('EMPLEADOS_IMPORTAR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cargarEmpleados(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.exito("Carga masiva procesada", empresaAdminService.cargarEmpleados(file)));
    }

    @GetMapping("/plantilla-credenciales")
    @PreAuthorize("@perm.has('CREDENCIALES_OPERAR')")
    public ResponseEntity<byte[]> descargarPlantillaCredenciales() {
        String csv = empresaAdminService.generarPlantillaCredencialesCsv();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_credenciales.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping(path = "/cargar-credenciales", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@perm.has('CREDENCIALES_OPERAR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cargarCredenciales(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.exito("Carga de credenciales procesada", empresaAdminService.cargarCredenciales(file)));
    }

    @PostMapping("/credenciales/lote-inicial")
    @PreAuthorize("@perm.has('CREDENCIALES_OPERAR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generarLoteInicial(@RequestBody Map<String, Object> payload) {
        Integer cantidad = payload.get("cantidad") != null ? Integer.valueOf(String.valueOf(payload.get("cantidad"))) : null;
        Long inicio = payload.get("inicio") != null ? Long.valueOf(String.valueOf(payload.get("inicio"))) : null;
        return ResponseEntity.ok(ApiResponse.exito("Lote inicial generado", empresaAdminService.generarLoteInicialCredenciales(cantidad, inicio)));
    }

    @PostMapping("/credenciales/asignacion-automatica")
    @PreAuthorize("@perm.has('CREDENCIALES_OPERAR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> asignacionAutomaticaCredenciales(@RequestBody(required = false) Map<String, Object> payload) {
        Integer limite = payload != null && payload.get("limite") != null
            ? Integer.valueOf(String.valueOf(payload.get("limite")))
            : 100;
        return ResponseEntity.ok(ApiResponse.exito(
            "Asignacion automatica completada",
            empresaAdminService.asignarCredencialesAutomaticamente(limite)
        ));
    }

    @PostMapping("/credenciales/asignacion-manual")
    @PreAuthorize("@perm.has('CREDENCIALES_OPERAR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> asignacionManualCredencial(@RequestBody Map<String, Object> payload) {
        String numeroCredencial = payload.get("numeroCredencial") != null ? payload.get("numeroCredencial").toString() : null;
        String numeroEmpleado = payload.get("numeroEmpleado") != null ? payload.get("numeroEmpleado").toString() : null;
        
        if (numeroCredencial == null || numeroCredencial.isBlank()) {
            throw new IllegalArgumentException("numeroCredencial es requerido");
        }
        if (numeroEmpleado == null || numeroEmpleado.isBlank()) {
            throw new IllegalArgumentException("numeroEmpleado es requerido");
        }
        
        return ResponseEntity.ok(ApiResponse.exito(
            "Credencial asignada exitosamente",
            empresaAdminService.asignarCredencialManualmente(numeroCredencial, numeroEmpleado)
        ));
    }

    @GetMapping({"/empleados/activos", "/empleados/sin-credencial"})
    @PreAuthorize("@perm.has('CREDENCIALES_OPERAR')")
    public ResponseEntity<ApiResponse<java.util.List<Map<String, Object>>>> obtenerEmpleadosActivos() {
        return ResponseEntity.ok(ApiResponse.exito(
            "Empleados activos obtenidos",
            empresaAdminService.obtenerEmpleadosActivos()
        ));
    }

    @GetMapping("/configuracion")
    @PreAuthorize("@perm.has('EMPRESA_CONFIG_VER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerConfiguracion() {
        return ResponseEntity.ok(ApiResponse.exito(empresaAdminService.obtenerConfiguracion()));
    }

    @PutMapping("/configuracion")
    @PreAuthorize("@perm.has('EMPRESA_CONFIG_EDITAR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> guardarConfiguracion(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(ApiResponse.exito("Configuracion guardada", empresaAdminService.guardarConfiguracion(payload)));
    }
}
