package com.efectivale.centrocostos.controller;

import com.efectivale.centrocostos.dto.ApiResponse;
import com.efectivale.centrocostos.dto.GrupoDto;
import com.efectivale.centrocostos.dto.GrupoListadoDto;
import com.efectivale.centrocostos.entity.Grupo;
import com.efectivale.centrocostos.entity.GrupoEmpleado;
import com.efectivale.centrocostos.service.GrupoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/grupos", "/Administracion_de_grupos"})
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    @GetMapping({"", "/"})
    public ResponseEntity<ApiResponse<List<GrupoListadoDto>>> listarCompat() {
        return ResponseEntity.ok(ApiResponse.exito(grupoService.listarGrupos()));
    }

    @RequestMapping(
            value = {
                    "/Asignar_grupos/v1/asig_grupo/getGrupos/",
                    "/Reporte_grupos/v1/reporte_grupo/getGrupos/",
                    "/Registrar_grupos/v1/reg_grupo/table/"
            },
            method = {RequestMethod.GET, RequestMethod.PUT}
    )
    public ResponseEntity<ApiResponse<List<GrupoListadoDto>>> listar() {
        return ResponseEntity.ok(ApiResponse.exito(grupoService.listarGrupos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Grupo>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.exito(grupoService.obtenerGrupo(id)));
    }

    @PostMapping({"", "/"})
    @PreAuthorize("@perm.has('GRUPOS_GESTIONAR')")
    public ResponseEntity<ApiResponse<Grupo>> registrar(@Valid @RequestBody GrupoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.creado(grupoService.registrarGrupo(dto)));
    }

    @RequestMapping(value = "/Registrar_grupos/v1/reg_grupo/modificar/", method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("@perm.has('GRUPOS_GESTIONAR')")
    public ResponseEntity<ApiResponse<Grupo>> registrar(@RequestBody Map<String, Object> body) {
        GrupoDto dto = new GrupoDto();
        dto.setGrupoid(value(body, "grupoid", value(body, "nombre", null)));
        dto.setNombre(dto.getGrupoid());
        dto.setDescripcion(value(body, "descripcion", null));
        dto.setCalle(value(body, "calle", null));
        dto.setNumero(value(body, "numero", null));
        dto.setColonia(value(body, "colonia", null));
        dto.setCodigopostal(value(body, "codigopostal", null));
        dto.setDelegacion(value(body, "delegacion", null));
        dto.setEstado(value(body, "estado", null));
        dto.setNombreContacto(value(body, "nombre", null));
        dto.setTelefono(value(body, "telefono", null));
        dto.setNombre2(value(body, "nombre2", null));
        dto.setTelefono2(value(body, "telefono2", null));
        dto.setHorario(value(body, "horario", null));
        dto.setObservacion(value(body, "observacion", null));
        dto.setEstatusid(parseBoolean(body.get("estatusid")));
        dto.setUsuarioAlta(value(body, "usuarioAlta", value(body, "usuario", null)));
        dto.setClienteId(parseLong(body.get("clienteId")));
        dto.setConsignatarioId(parseLong(body.get("consignatarioId")));

        String action = value(body, "action", "Registrar");
        Long idDirecciones = parseLong(body.get("iddirecciones"));

        if ("Actualizar".equalsIgnoreCase(action)) {
            if (idDirecciones == null) {
                throw new IllegalArgumentException("iddirecciones es obligatorio para actualizar");
            }
            return ResponseEntity.ok(ApiResponse.exito("Grupo actualizado exitosamente", grupoService.actualizarGrupo(idDirecciones, dto)));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.creado(grupoService.registrarGrupo(dto)));
    }

    @PostMapping("/{idGrupo}/empleados")
    @PreAuthorize("@perm.has('GRUPOS_ASIGNAR')")
    public ResponseEntity<ApiResponse<GrupoEmpleado>> asignarEmpleado(
            @PathVariable Long idGrupo,
            @RequestBody Map<String, Object> body) {
        Long idEmpleado = Long.valueOf(body.get("idEmpleado").toString());
        String numeroEmpleado = (String) body.get("numeroEmpleado");
        String usuarioAsigno = (String) body.get("usuarioAsigno");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.creado(
                grupoService.asignarEmpleado(idGrupo, idEmpleado, numeroEmpleado, usuarioAsigno)));
    }

    @RequestMapping(value = "/Asignar_grupos/v1/asig_grupo/asignar/", method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("@perm.has('GRUPOS_ASIGNAR')")
    public ResponseEntity<ApiResponse<GrupoEmpleado>> asignarEmpleado(@RequestBody Map<String, Object> body) {
        Long idGrupo = body.get("idGrupo") != null ? Long.valueOf(body.get("idGrupo").toString()) : null;
        Long idEmpleado = body.get("idEmpleado") != null ? Long.valueOf(body.get("idEmpleado").toString()) : null;
        String numeroEmpleado = body.get("numeroEmpleado") != null ? body.get("numeroEmpleado").toString() : null;
        String usuarioAsigno = body.get("usuarioAsigno") != null ? body.get("usuarioAsigno").toString() : null;

        if (idGrupo == null || idEmpleado == null) {
            throw new IllegalArgumentException("idGrupo e idEmpleado son obligatorios");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.creado(
                grupoService.asignarEmpleado(idGrupo, idEmpleado, numeroEmpleado, usuarioAsigno)));
    }

    @GetMapping("/{idGrupo}/reporte")
    @PreAuthorize("@perm.has('GRUPOS_REPORTE')")
    public ResponseEntity<ApiResponse<List<GrupoEmpleado>>> reporte(@PathVariable Long idGrupo) {
        return ResponseEntity.ok(ApiResponse.exito(grupoService.reporteGrupo(idGrupo)));
    }

    @RequestMapping(value = "/Reporte_grupos/v1/reporte_grupo/getListChildCC/", method = {RequestMethod.GET, RequestMethod.PUT})
    @PreAuthorize("@perm.has('GRUPOS_REPORTE')")
    public ResponseEntity<ApiResponse<List<GrupoEmpleado>>> reporteCompat(@RequestParam Long idGrupo) {
        return ResponseEntity.ok(ApiResponse.exito(grupoService.reporteGrupo(idGrupo)));
    }

        @GetMapping("/Registrar_grupos/v1/reg_grupo/getExcel")
        public ResponseEntity<byte[]> getExcelGrupos() {
        String csv = "iddirecciones,grupoid,descripcion,calle,numero,colonia,codigopostal,delegacion,estado,nombre,telefono,nombre2,telefono2,horario,estatus,fecha,observacion\n" +
            grupoService.listarGrupos().stream()
                .map(g -> String.join(",",
                    String.valueOf(g.getIddirecciones()),
                    safe(g.getGrupoid()),
                    safe(g.getDescripcion()),
                    safe(g.getCalle()),
                    safe(g.getNumero()),
                    safe(g.getColonia()),
                    safe(g.getCodigopostal()),
                    safe(g.getDelegacion()),
                    safe(g.getEstado()),
                    safe(g.getNombre()),
                    safe(g.getTelefono()),
                    safe(g.getNombre2()),
                    safe(g.getTelefono2()),
                    safe(g.getHorario()),
                    String.valueOf(g.getEstatus()),
                    g.getFecha() != null ? g.getFecha().toString() : "",
                    safe(g.getObservacion())))
                    .collect(Collectors.joining("\n"));

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grupos_.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv.getBytes(StandardCharsets.UTF_8));
        }

        @GetMapping("/Asignar_grupos/v1/asig_grupo/getExcel")
        public ResponseEntity<byte[]> getExcelAsignacion(@RequestParam(required = false) String tipo) {
        String csv = "tipo,mensaje\n" + safe(tipo) + ",Plantilla de asignacion ";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_asignacion_.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv.getBytes(StandardCharsets.UTF_8));
        }

        @PostMapping("/Asignar_grupos/v1/asig_grupo/procesarExcel")
        @PreAuthorize("@perm.has('GRUPOS_ASIGNAR')")
        public ResponseEntity<ApiResponse<Map<String, Object>>> procesarExcelAsignacion(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String b64) {
        return ResponseEntity.ok(ApiResponse.exito(Map.of(
            "procesado", true,
            "mensaje", "Archivo de asignacion procesado en modo ",
            "user", user,
            "archivo", b64 != null ? "recibido" : "vacio"
        )));
        }

        @GetMapping("/Reporte_grupos/v1/reporte_grupo/getExcel")
        @PreAuthorize("@perm.has('GRUPOS_REPORTE')")
        public ResponseEntity<byte[]> getExcelReporte(@RequestParam(required = false) Long idGrupo) {
        List<GrupoEmpleado> data = idGrupo != null ? grupoService.reporteGrupo(idGrupo) : List.of();
        String csv = "numeroEmpleado,activo,fechaAsignacion,usuarioAsigno\n" +
            data.stream()
                .map(e -> String.join(",",
                    safe(e.getNumeroEmpleado()),
                        String.valueOf(e.getActivo()),
                    e.getFechaAsignacion() != null ? e.getFechaAsignacion().toString() : "",
                    safe(e.getUsuarioAsigno())))
                    .collect(Collectors.joining("\n"));

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_grupos_.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv.getBytes(StandardCharsets.UTF_8));
        }

        private String safe(String val) {
        return val == null ? "" : val.replace(",", " ").trim();
        }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private Boolean parseBoolean(Object value) {
        if (value == null) {
            return null;
        }
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            return null;
        }
        return "true".equalsIgnoreCase(raw) || "1".equals(raw) || "si".equalsIgnoreCase(raw);
    }

    private String value(Map<String, Object> body, String key, String fallback) {
        Object val = body.get(key);
        return val != null ? String.valueOf(val) : fallback;
    }
}

