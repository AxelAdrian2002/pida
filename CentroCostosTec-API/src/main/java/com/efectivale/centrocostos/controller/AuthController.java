package com.efectivale.centrocostos.controller;

import com.efectivale.centrocostos.dto.ApiResponse;
import com.efectivale.centrocostos.dto.LoginDto;
import com.efectivale.centrocostos.dto.RegistroEmpresaDto;
import com.efectivale.centrocostos.service.AuthService;
import com.efectivale.centrocostos.service.CloudinaryStorageService;
import com.efectivale.centrocostos.service.CuentaVerificacionService;
import com.efectivale.centrocostos.service.RegistroEmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping({"/auth", "/Login/login/v1"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CuentaVerificacionService cuentaVerificacionService;
    private final RegistroEmpresaService registroEmpresaService;
    private final CloudinaryStorageService cloudinaryStorageService;

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

    @PostMapping(path = {"/media/upload", "/media/upload/"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> subirImagenPublica(@RequestParam("file") MultipartFile file) {
        Map<String, String> subida = cloudinaryStorageService.uploadImage(file, "registro");
        return ResponseEntity.ok(ApiResponse.exito("Imagen subida correctamente", subida));
    }
}
