package com.efectivale.centrocostos.service;

import com.efectivale.centrocostos.security.ContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TenantAuditService {

    private final JdbcTemplate jdbc;

    public TenantAuditService(@Qualifier("dbdespensaJdbc") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void logFromContext(String modulo, String accion, String detalle, ContextProvider contextProvider) {
        Long usuarioId = safeLong(contextProvider::getIdUsuario);
        String username = safeString(contextProvider::getUsername);
        String corporativoId = safeString(contextProvider::getCorporativoId);
        Long clienteId = safeLong(contextProvider::getClienteId);
        Long consignatarioId = safeLong(contextProvider::getConsignatarioId);

        log(modulo, accion, detalle, usuarioId, username, corporativoId, clienteId, consignatarioId);
    }

    public void log(String modulo,
                    String accion,
                    String detalle,
                    Long usuarioId,
                    String username,
                    String corporativoId,
                    Long clienteId,
                    Long consignatarioId) {
        try {
            jdbc.update(
                "INSERT INTO tenant_audit_log (modulo, accion, detalle, usuarioid, username, corporativoid, clienteid, consignatarioid) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                modulo,
                accion,
                detalle,
                usuarioId,
                username,
                corporativoId,
                clienteId,
                consignatarioId
            );
        } catch (Exception ex) {
            // La auditoria no debe romper el flujo principal.
            log.warn("No se pudo registrar auditoria tenant: {} {}", modulo, accion, ex);
        }
    }

    private Long safeLong(SupplierWithException<Long> supplier) {
        try {
            return supplier.get();
        } catch (Exception ex) {
            return null;
        }
    }

    private String safeString(SupplierWithException<String> supplier) {
        try {
            return supplier.get();
        } catch (Exception ex) {
            return null;
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
