package com.efectivale.centrocostos.controller;

import com.efectivale.centrocostos.dto.ApiResponse;
import com.efectivale.centrocostos.dto.DatabaseStatusDto;
import com.efectivale.centrocostos.dto.ModuloCoberturaDto;
import com.efectivale.centrocostos.service.DatabaseCoverageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bases")
@RequiredArgsConstructor
public class DatabaseCoverageController {

    private final DatabaseCoverageService databaseCoverageService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        List<DatabaseStatusDto> bases = databaseCoverageService.obtenerEstatusBases();
        List<ModuloCoberturaDto> cobertura = databaseCoverageService.obtenerCoberturaModulos();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("bases", bases);
        payload.put("cobertura", cobertura);

        return ResponseEntity.ok(ApiResponse.exito("Estatus de bases recuperado", payload));
    }
}
