package com.efectivale.centrocostos.controller;

import com.efectivale.centrocostos.dto.ApiResponse;
import com.efectivale.centrocostos.dto.CredencialOperacionDto;
import com.efectivale.centrocostos.entity.Credencial;
import com.efectivale.centrocostos.entity.CredencialBitacora;
import com.efectivale.centrocostos.service.CredencialService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import java.nio.charset.StandardCharsets;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CredencialController {

    private final CredencialService credencialService;
    private final ObjectMapper objectMapper;

    @GetMapping({"/credenciales", "/tarjetas", "/Administracion_Tarjetas/tarjetas/v1/getTarjetas"})
    public ResponseEntity<ApiResponse<Page<Credencial>>> consultar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long idGrupo,
            @RequestParam(required = false) String numeroEmpleado,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.exito(
                credencialService.consultarCredenciales(estado, idGrupo, numeroEmpleado, pageable)));
    }

    @GetMapping({"/credenciales/{numeroCredencial}", "/tarjetas/{numeroTarjeta}", "/Administracion_Tarjetas/tarjetas/v1/getTarjeta/{numeroTarjeta}"})
    public ResponseEntity<ApiResponse<Credencial>> obtener(
            @PathVariable(required = false) String numeroCredencial,
            @PathVariable(required = false) String numeroTarjeta) {
        String numero = numeroCredencial != null ? numeroCredencial : numeroTarjeta;
        return ResponseEntity.ok(ApiResponse.exito(credencialService.obtenerPorNumero(numero)));
    }

    @GetMapping({"/credenciales/{idCredencial}/bitacora", "/tarjetas/{idTarjeta}/bitacora", "/Administracion_Tarjetas/tarjetas/v1/getBitacora/{idTarjeta}"})
    public ResponseEntity<ApiResponse<List<CredencialBitacora>>> bitacora(
            @PathVariable(required = false) Long idCredencial,
            @PathVariable(required = false) Long idTarjeta) {
        Long id = idCredencial != null ? idCredencial : idTarjeta;
        return ResponseEntity.ok(ApiResponse.exito(credencialService.obtenerBitacora(id)));
    }

    @PutMapping({"/credenciales/activar", "/tarjetas/activar"})
    @PreAuthorize("@perm.has('CREDENCIALES_OPERAR')")
    public ResponseEntity<ApiResponse<Credencial>> activar(@Valid @RequestBody CredencialOperacionDto dto) {
        return ResponseEntity.ok(ApiResponse.exito("Credencial activada exitosamente",
                credencialService.activarCredencial(dto)));
    }

    @PutMapping({"/credenciales/inactivar", "/tarjetas/inactivar"})
    @PreAuthorize("@perm.has('CREDENCIALES_OPERAR')")
    public ResponseEntity<ApiResponse<Credencial>> inactivar(@Valid @RequestBody CredencialOperacionDto dto) {
        return ResponseEntity.ok(ApiResponse.exito("Credencial inactivada exitosamente",
                credencialService.inactivarCredencial(dto)));
    }

    @PutMapping({"/credenciales/cancelar", "/tarjetas/cancelar"})
    @PreAuthorize("@perm.has('CREDENCIALES_OPERAR')")
    public ResponseEntity<ApiResponse<Credencial>> cancelar(@Valid @RequestBody CredencialOperacionDto dto) {
        return ResponseEntity.ok(ApiResponse.exito("Credencial cancelada exitosamente",
                credencialService.cancelarCredencial(dto)));
    }

    @PostMapping({"/credenciales/acciones/v1/accion", "/Administracion_Tarjetas/acciones/v1/accion"})
    @PreAuthorize("@perm.has('CREDENCIALES_OPERAR')")
    public ResponseEntity<ApiResponse<Credencial>> accion(@RequestBody Map<String, Object> body,
                                                             HttpServletRequest request) {
        String action = body.get("action") != null ? body.get("action").toString() : "";
        CredencialOperacionDto dto = new CredencialOperacionDto();
        dto.setNumeroCredencial(body.get("tarjetaid") != null ? body.get("tarjetaid").toString() : null);
        dto.setIdUsuario(body.get("usuarioId") != null ? Long.valueOf(body.get("usuarioId").toString()) : null);
        dto.setUsuarioOperacion(body.get("usuarioOperacion") != null ? body.get("usuarioOperacion").toString() : null);
        dto.setObservacion(body.get("observacion") != null ? body.get("observacion").toString() : null);
        dto.setClienteId(body.get("clienteid") != null ? Long.valueOf(body.get("clienteid").toString()) : null);
        dto.setConsignatarioId(body.get("consignatarioid") != null ? Long.valueOf(body.get("consignatarioid").toString()) : null);
        Object bitacoraId = request.getAttribute("bitacoraid");
        if (body.get("bitacoraId") != null) {
            dto.setBitacoraId(Integer.valueOf(body.get("bitacoraId").toString()));
        } else if (bitacoraId instanceof Integer bitacora) {
            dto.setBitacoraId(bitacora);
        } else {
            dto.setBitacoraId(-1);
        }

        if ("Activar".equalsIgnoreCase(action)) {
            return ResponseEntity.ok(ApiResponse.exito("Credencial activada exitosamente", credencialService.activarCredencial(dto)));
        }
        if ("Inactivar".equalsIgnoreCase(action)) {
            return ResponseEntity.ok(ApiResponse.exito("Credencial inactivada exitosamente", credencialService.inactivarCredencial(dto)));
        }
        if ("Cancelar".equalsIgnoreCase(action)) {
            return ResponseEntity.ok(ApiResponse.exito("Credencial cancelada exitosamente", credencialService.cancelarCredencial(dto)));
        }
        throw new IllegalArgumentException("Accion no soportada: " + action);
    }

    @GetMapping({"/credenciales/excel", "/Administracion_Tarjetas/excel/v1/getExcel"})
    public ResponseEntity<byte[]> getExcel(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String data,
            @PageableDefault(size = 1000) Pageable pageable) {
        String tipoFinal = tipo;
        if ((tipoFinal == null || tipoFinal.isBlank()) && data != null && !data.isBlank()) {
            try {
                Map<String, Object> payload = objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
                Object tipoDesdeData = payload.get("tipo");
                if (tipoDesdeData != null) {
                    tipoFinal = tipoDesdeData.toString();
                }
            } catch (Exception ignored) {
                // Si data no es JSON válido, se continúa con defaults.
            }
        }

        Page<Credencial> page = credencialService.consultarCredenciales(null, null, null, pageable);
        String csv = "numeroCredencial,numeroEmpleado,nombreEmpleado,estado\n" +
                page.getContent().stream()
                        .map(t -> String.join(",",
                    safe(t.getNumeroCredencial()),
                                safe(t.getNumeroEmpleado()),
                                safe(t.getNombreEmpleado()),
                                safe(t.getEstado())))
                .collect(Collectors.joining("\n"));

        String fileName = (tipoFinal != null && !tipoFinal.isBlank()) ? "credenciales_" + tipoFinal.toLowerCase() + ".csv" : "credenciales.csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/Administracion_Tarjetas/cancelacion_masiva/v1/cancelacion")
    @PreAuthorize("@perm.has('CREDENCIALES_OPERAR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelacionMasiva(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String b64) {
        return ResponseEntity.ok(ApiResponse.exito(Map.of(
                "procesado", true,
                "mensaje", "Cancelacion masiva procesada en modo ",
                "user", user,
                "archivo", b64 != null ? "recibido" : "vacio"
        )));
    }

    private String safe(String val) {
        return val == null ? "" : val.replace(",", " ").trim();
    }
}

