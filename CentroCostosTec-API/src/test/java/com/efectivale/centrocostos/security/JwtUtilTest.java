package com.efectivale.centrocostos.security;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 3_600_000L);
    }

    @Test
    void shouldGenerateAndReadExtendedClaims() {
        String token = jwtUtil.generateToken(
            "usuario_demo",
            "ADMIN",
            10L,
            20L,
            30L,
            "CORP-01",
            "CEN-01",
            List.of("SOLICITUDES_AUTORIZAR", "CREDENCIALES_OPERAR")
        );

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("usuario_demo", jwtUtil.getUsernameFromToken(token));
        assertEquals("ADMIN", jwtUtil.getRolFromToken(token));
        assertEquals(10L, jwtUtil.getIdUsuarioFromToken(token));
        assertEquals(20L, jwtUtil.getClienteIdFromToken(token));
        assertEquals(30L, jwtUtil.getConsignatarioIdFromToken(token));
        assertEquals("CORP-01", jwtUtil.getCorporativoIdFromToken(token));
        assertEquals("CEN-01", jwtUtil.getCentroIdFromToken(token));
        assertEquals(2, jwtUtil.getPermisosFromToken(token).size());
    }

    @Test
    void shouldInvalidateTamperedToken() {
        String token = jwtUtil.generateToken("demo", "ADMIN");
        String tampered = token + "abc";

        assertFalse(jwtUtil.validateToken(tampered));
    }
}
