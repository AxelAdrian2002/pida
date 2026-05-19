package com.efectivale.centrocostos.controller;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efectivale.centrocostos.security.JwtUtil;

import lombok.RequiredArgsConstructor;

/**
 * Endpoint de testing para generar tokens JWT válidos sin requerir BD de usuario.
 * Solo activo cuando spring.profiles.active=dev o app.testing.enabled=true
 */
@RestController
@RequestMapping("/testing")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.testing.enabled", havingValue = "true", matchIfMissing = true)
public class TestingController {

    private final JwtUtil jwtUtil;

    /**
     * Genera un JWT válido para pruebas sin requerer credenciales.
     * Retorna token con claims ADMIN para acceso completo.
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, Object>> generateTestToken() {
        String token = jwtUtil.generateToken(
            "testuser",
            "ADMIN",
            10001L,               // idUsuario (Andrea Lopez - colaborador real en BD)
            1001L,                // clienteId (unidad OPER-100 en BD)
            2001L,                // consignatarioId (unidad OPER-100 en BD)
            "NOVA01",             // corporativoId (empresa_operativa en BD)
            "OPER-100",           // centroId (unidad_operativa en BD)
            java.util.List.of()   // permisos
        );

        return ResponseEntity.ok(Map.ofEntries(
            Map.entry("token", token),
            Map.entry("username", "testuser"),
            Map.entry("rol", "ADMIN"),
            Map.entry("idUsuario", 10001),
            Map.entry("clienteId", 1001),
            Map.entry("consignatarioId", 2001),
            Map.entry("corporativoId", "NOVA01"),
            Map.entry("centroId", "OPER-100"),
            Map.entry("valid_for_seconds", 86400)
        ));
    }

    /**
     * Genera token para usuario empleado (solo puede crear solicitudes).
     */
    @GetMapping("/token/empleado")
    public ResponseEntity<Map<String, Object>> generateEmpleadoToken() {
        String token = jwtUtil.generateToken(
            "empleado_test",
            "EMPLEADO",
            10002L,
            1001L,
            2001L,
            "NOVA01",
            "OPER-100",
            java.util.List.of()
        );

        return ResponseEntity.ok(Map.ofEntries(
            Map.entry("token", token),
            Map.entry("role", "EMPLEADO")
        ));
    }

    /**
     * Genera token para usuario CAPTURA.
     */
    @GetMapping("/token/captura")
    public ResponseEntity<Map<String, Object>> generateCapturaToken() {
        String token = jwtUtil.generateToken(
            "captura_test",
            "CAPTURA",
            10003L,
            1001L,
            2001L,
            "NOVA01",
            "OPER-100",
            java.util.List.of()
        );

        return ResponseEntity.ok(Map.ofEntries(
            Map.entry("token", token),
            Map.entry("role", "CAPTURA")
        ));
    }

    /**
     * Token para OPER-200 (segunda unidad operativa, clienteId=1002, consignatarioId=2002).
     * Sirve para probar solicitudes en modo multiempresa.
     */
    @GetMapping("/token/oper200")
    public ResponseEntity<Map<String, Object>> generateOper200Token() {
        String token = jwtUtil.generateToken(
            "admin_oper200",
            "ADMIN",
            10004L,
            1002L,
            2002L,
            "NOVA01",
            "OPER-200",
            java.util.List.of()
        );

        return ResponseEntity.ok(Map.ofEntries(
            Map.entry("token", token),
            Map.entry("username", "admin_oper200"),
            Map.entry("rol", "ADMIN"),
            Map.entry("idUsuario", 10004),
            Map.entry("clienteId", 1002),
            Map.entry("consignatarioId", 2002),
            Map.entry("corporativoId", "NOVA01"),
            Map.entry("centroId", "OPER-200"),
            Map.entry("saldoMonedero", 98000.00),
            Map.entry("saldoCredito", 21000.00),
            Map.entry("valid_for_seconds", 86400)
        ));
    }
}
