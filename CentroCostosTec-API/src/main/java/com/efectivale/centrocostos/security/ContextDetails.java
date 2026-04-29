package com.efectivale.centrocostos.security;

import lombok.Getter;

/**
 * Detalles del contexto de autenticación que se almacenan en Authentication.details.
 * Contiene información de cliente, consignatario e idUsuario del JWT.
 */
@Getter
public class ContextDetails {
    private Long idUsuario;
    private Long clienteId;
    private Long consignatarioId;
    private String corporativoId;
    private String centroId;

    public ContextDetails(Long idUsuario, Long clienteId, Long consignatarioId, String corporativoId, String centroId) {
        this.idUsuario = idUsuario;
        this.clienteId = clienteId;
        this.consignatarioId = consignatarioId;
        this.corporativoId = corporativoId;
        this.centroId = centroId;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getConsignatarioId() {
        return consignatarioId;
    }

    public String getCorporativoId() {
        return corporativoId;
    }

    public String getCentroId() {
        return centroId;
    }
}
