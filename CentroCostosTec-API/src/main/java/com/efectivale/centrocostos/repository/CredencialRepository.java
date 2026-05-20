package com.efectivale.centrocostos.repository;

import com.efectivale.centrocostos.entity.Credencial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CredencialRepository extends JpaRepository<Credencial, Long> {

    @Query(value = "SELECT t.*, " +
            "COALESCE(p.parametrosactiva, FALSE) AS parametrosactiva " +
            "FROM credencial_interna t " +
            "LEFT JOIN estado_credencial p ON p.parametrosid = t.parametrosid " +
            "WHERE CAST(t.tarjetaid AS VARCHAR) = :numeroCredencial AND t.clienteid = :clienteId AND t.consignatarioid = :consignatarioId LIMIT 1",
            nativeQuery = true)
    Optional<Credencial> findByNumeroCredencial(@Param("numeroCredencial") String numeroCredencial,
                                                @Param("clienteId") Long clienteId,
                                                @Param("consignatarioId") Long consignatarioId);

    @Query(value = "SELECT t.*, " +
            "COALESCE(p.parametrosactiva, FALSE) AS parametrosactiva " +
            "FROM credencial_interna t " +
            "LEFT JOIN estado_credencial p ON p.parametrosid = t.parametrosid " +
            "WHERE (:estado IS NULL OR :estado = '' OR " +
            "      (:estado = 'CANCELADA' AND t.tarjetacancelada = TRUE) OR " +
            "      (:estado = 'ACTIVA' AND t.tarjetacancelada = FALSE AND p.parametrosactiva IS TRUE) OR " +
            "      (:estado = 'INACTIVA' AND t.tarjetacancelada = FALSE AND COALESCE(p.parametrosactiva, FALSE) = FALSE)) " +
            "AND (:numeroEmpleado IS NULL OR :numeroEmpleado = '' OR t.empleadoid LIKE CONCAT('%', :numeroEmpleado, '%')) " +
            "AND t.clienteid = :clienteId AND t.consignatarioid = :consignatarioId " +
            "ORDER BY t.tarjetaid DESC",
            countQuery = "SELECT COUNT(*) " +
            "FROM credencial_interna t " +
            "LEFT JOIN estado_credencial p ON p.parametrosid = t.parametrosid " +
            "WHERE (:estado IS NULL OR :estado = '' OR " +
            "      (:estado = 'CANCELADA' AND t.tarjetacancelada = TRUE) OR " +
            "      (:estado = 'ACTIVA' AND t.tarjetacancelada = FALSE AND p.parametrosactiva IS TRUE) OR " +
            "      (:estado = 'INACTIVA' AND t.tarjetacancelada = FALSE AND COALESCE(p.parametrosactiva, FALSE) = FALSE)) " +
            "AND (:numeroEmpleado IS NULL OR :numeroEmpleado = '' OR t.empleadoid LIKE CONCAT('%', :numeroEmpleado, '%')) " +
            "AND t.clienteid = :clienteId AND t.consignatarioid = :consignatarioId",
            nativeQuery = true)
    Page<Credencial> buscarConFiltros(@Param("estado") String estado,
                                      @Param("numeroEmpleado") String numeroEmpleado,
                                      @Param("clienteId") Long clienteId,
                                      @Param("consignatarioId") Long consignatarioId,
                                      Pageable pageable);
}
