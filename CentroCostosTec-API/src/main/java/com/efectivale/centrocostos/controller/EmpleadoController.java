package com.efectivale.centrocostos.controller;

import com.efectivale.centrocostos.dto.ApiResponse;
import com.efectivale.centrocostos.dto.EmpleadoDto;
import com.efectivale.centrocostos.entity.Empleado;
import com.efectivale.centrocostos.service.EmpleadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/empleados", "/Actualizar_Datos/datosEmpleado/v1/datosEmpleado"})
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;

        @GetMapping({"", "/"})
    public ResponseEntity<ApiResponse<Page<Empleado>>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String numeroEmpleado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.exito(
                empleadoService.consultarEmpleados(nombre, numeroEmpleado != null ? numeroEmpleado : departamento, pageable)));
    }

        @RequestMapping(value = "/getDataExcel/", method = {RequestMethod.GET, RequestMethod.PUT})
        public ResponseEntity<ApiResponse<Page<Empleado>>> listarDataExcel(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String numeroEmpleado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.exito(
            empleadoService.consultarEmpleados(nombre, numeroEmpleado != null ? numeroEmpleado : departamento, pageable)));
        }

        @GetMapping("/centrocostos/")
        public ResponseEntity<ApiResponse<Page<Empleado>>> centrosCostos(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.exito(
            empleadoService.consultarEmpleados(null, null, pageable)));
        }

    @GetMapping("/{numeroEmpleado}")
    public ResponseEntity<ApiResponse<Empleado>> obtener(@PathVariable String numeroEmpleado) {
        return ResponseEntity.ok(ApiResponse.exito(empleadoService.obtenerPorNumero(numeroEmpleado)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Empleado>> alta(@Valid @RequestBody EmpleadoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.creado(empleadoService.altaEmpleado(dto)));
    }

    @PutMapping("/{numeroEmpleado}")
    @PreAuthorize("hasAnyRole('ADMIN','CAPTURA')")
    public ResponseEntity<ApiResponse<Empleado>> actualizar(
            @PathVariable String numeroEmpleado,
            @Valid @RequestBody EmpleadoDto dto) {
        return ResponseEntity.ok(ApiResponse.exito("Empleado actualizado exitosamente",
                empleadoService.actualizarEmpleado(numeroEmpleado, dto)));
    }

        @GetMapping("/getExcel")
        public ResponseEntity<byte[]> getExcel(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String numeroEmpleado) {
        String csv = "numeroEmpleado,nombre,telefono,email\n" +
            empleadoService.consultarEmpleados(nombre, numeroEmpleado, Pageable.ofSize(1000)).getContent().stream()
                .map(e -> String.join(",",
                    safe(e.getNumeroEmpleado()),
                    safe(e.getNombre() + " " + safe(e.getApellidoPaterno())),
                    safe(e.getTelefono()),
                    safe(e.getEmail())))
                .reduce("", (a, b) -> a + (a.isEmpty() ? "" : "\n") + b);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=datos_empleados_.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv.getBytes(StandardCharsets.UTF_8));
        }

        @PostMapping("/procesarExcel/")
        public ResponseEntity<ApiResponse<Map<String, Object>>> procesarExcel(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String b64) {
        return ResponseEntity.ok(ApiResponse.exito(Map.of(
            "procesado", true,
            "mensaje", "Archivo procesado en modo ",
            "user", user,
            "archivo", b64 != null ? "recibido" : "vacio"
        )));
        }

        private String safe(String val) {
        return val == null ? "" : val.replace(",", " ").trim();
        }
}

