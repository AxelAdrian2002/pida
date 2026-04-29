package com.efectivale.centrocostos.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username, String rol) {
        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * VersiÃ³n extendida con claims de negocio equivalentes al .
     * Incluye clienteId, consignatarioId, corporativoId y centroId.
     */
    public String generateToken(String username, String rol,
                                long idUsuario, long clienteId, long consignatarioId,
                                String corporativoId, String centroId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("rol",             rol)
                .claim("idUsuario",       idUsuario)
                .claim("clienteId",       clienteId)
                .claim("consignatarioId", consignatarioId)
                .claim("corporativoId",   corporativoId)
                .claim("centroId",        centroId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRolFromToken(String token) {
        return (String) parseClaims(token).get("rol");
    }

    public Long getClienteIdFromToken(String token) {
        Object val = parseClaims(token).get("clienteId");
        return val != null ? ((Number) val).longValue() : null;
    }

    public Long getConsignatarioIdFromToken(String token) {
        Object val = parseClaims(token).get("consignatarioId");
        return val != null ? ((Number) val).longValue() : null;
    }

    public Long getIdUsuarioFromToken(String token) {
        Object val = parseClaims(token).get("idUsuario");
        return val != null ? ((Number) val).longValue() : null;
    }

    public String getCorporativoIdFromToken(String token) {
        return (String) parseClaims(token).get("corporativoId");
    }

    public String getCentroIdFromToken(String token) {
        return (String) parseClaims(token).get("centroId");
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}


