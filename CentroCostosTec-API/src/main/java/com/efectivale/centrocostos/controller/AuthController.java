package com.efectivale.centrocostos.controller;

import com.efectivale.centrocostos.dto.ApiResponse;
import com.efectivale.centrocostos.dto.LoginDto;
import com.efectivale.centrocostos.service.AuthService;
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

    @PostMapping({"/login", "/login/"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginDto dto) {
        Map<String, Object> resultado = authService.login(dto);
        return ResponseEntity.ok(ApiResponse.exito("Login exitoso", resultado));
    }
}
