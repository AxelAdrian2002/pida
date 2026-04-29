package com.efectivale.centrocostos.controller;

import com.efectivale.centrocostos.dto.ApiResponse;
import com.efectivale.centrocostos.dto.CambioPasswordDto;
import com.efectivale.centrocostos.service.CambioPasswordService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Administracion_Login")
@RequiredArgsConstructor
public class CambioPasswordController {

    private final CambioPasswordService cambioPasswordService;

    @PutMapping({
            "/cambio_contrasenia/v1/cambio_contrasenia",
            "/cambio_contrasenia/v1/cambio_contrasenia/"
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePassword(@Valid @RequestBody CambioPasswordDto dto) {
        String mensaje = cambioPasswordService.cambiarPassword(dto);
        return ResponseEntity.ok(ApiResponse.exito(mensaje, Map.of(
                "actualizado", true,
                "mensaje", mensaje
        )));
    }
}

