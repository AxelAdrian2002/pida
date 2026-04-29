package com.efectivale.centrocostos.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Proveedor de contexto de sesión para obtener clienteId y consignatarioId
 * del usuario autenticado. Estos valores se extraen del JWT via Claims.
 */
@Component
public class ContextProvider {

    /**
     * Obtiene el clienteId del usuario autenticado desde el contexto de seguridad.
     * 
     * @return clienteId de la sesión, o null si no está disponible
     * @throws IllegalStateException si no hay sesión autenticada
     */
    public Long getClienteId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getDetails() == null) {
            throw new IllegalStateException("No hay sesión autenticada disponible");
        }
        
        Object details = auth.getDetails();
        if (details instanceof ContextDetails) {
            return ((ContextDetails) details).getClienteId();
        }
        
        return null;
    }

    /**
     * Obtiene el consignatarioId del usuario autenticado desde el contexto de seguridad.
     * 
     * @return consignatarioId de la sesión, o null si no está disponible
     * @throws IllegalStateException si no hay sesión autenticada
     */
    public Long getConsignatarioId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getDetails() == null) {
            throw new IllegalStateException("No hay sesión autenticada disponible");
        }
        
        Object details = auth.getDetails();
        if (details instanceof ContextDetails) {
            return ((ContextDetails) details).getConsignatarioId();
        }
        
        return null;
    }

    /**
     * Obtiene el idUsuario del usuario autenticado.
     * 
     * @return idUsuario de la sesión, o null si no está disponible
     */
    public Long getIdUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getDetails() == null) {
            return null;
        }
        
        Object details = auth.getDetails();
        if (details instanceof ContextDetails) {
            return ((ContextDetails) details).getIdUsuario();
        }
        
        return null;
    }

    /**
     * Obtiene el username del usuario autenticado.
     * 
     * @return username, o null si no está disponible
     */
    public String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    /**
     * Verifica que el clienteId/consignatarioId del DTO coincidan con la sesión.
     * Lanza excepción si no coinciden (intento de escalamiento de privilegios).
     * 
     * @param dtoClienteId clienteId del DTO
     * @param dtoCconsignatarioId consignatarioId del DTO
     * @throws IllegalArgumentException si los valores no coinciden
     */
    public void validarContextoCoincida(Long dtoClienteId, Long dtoCconsignatarioId) {
        Long sessionClienteId = getClienteId();
        Long sessionConsignatarioId = getConsignatarioId();
        
        if (dtoClienteId != null && !dtoClienteId.equals(sessionClienteId)) {
            throw new IllegalArgumentException(
                "No tiene permisos para acceder a este cliente. " +
                "ClienteId en sesión: " + sessionClienteId + 
                ", clienteId solicitado: " + dtoClienteId
            );
        }
        
        if (dtoCconsignatarioId != null && !dtoCconsignatarioId.equals(sessionConsignatarioId)) {
            throw new IllegalArgumentException(
                "No tiene permisos para acceder a este consignatario. " +
                "ConsignatarioId en sesión: " + sessionConsignatarioId + 
                ", consignatarioId solicitado: " + dtoCconsignatarioId
            );
        }
    }
}
