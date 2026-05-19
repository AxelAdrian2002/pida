package com.efectivale.centrocostos.controller;

import com.efectivale.centrocostos.dto.ApiResponse;
import com.efectivale.centrocostos.dto.LoginDto;
import com.efectivale.centrocostos.dto.RegistroEmpresaDto;
import com.efectivale.centrocostos.service.AuthService;
import com.efectivale.centrocostos.service.CuentaVerificacionService;
import com.efectivale.centrocostos.service.RegistroEmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping({"/auth", "/Login/login/v1"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CuentaVerificacionService cuentaVerificacionService;
    private final RegistroEmpresaService registroEmpresaService;

    @PostMapping({"/login", "/login/"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginDto dto) {
        Map<String, Object> resultado = authService.login(dto);
        return ResponseEntity.ok(ApiResponse.exito("Login exitoso", resultado));
    }

    @GetMapping({"/verificar-cuenta", "/verificar-cuenta/"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> verificarCuenta(@RequestParam("token") String token) {
        Map<String, Object> resultado = cuentaVerificacionService.verificarCuenta(token);
        return ResponseEntity.ok(ApiResponse.exito("Cuenta verificada correctamente", resultado));
    }

    @PostMapping({"/registrar-empresa", "/registrar-empresa/"})
    public ResponseEntity<ApiResponse<Map<String, String>>> registrarEmpresa(@Valid @RequestBody RegistroEmpresaDto dto) {
        Map<String, String> resultado = registroEmpresaService.registrarEmpresa(dto);
        return ResponseEntity.ok(ApiResponse.exito("Empresa registrada exitosamente", resultado));
    }
}
