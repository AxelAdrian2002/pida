package com.efectivale.centrocostos.repository;

import com.efectivale.centrocostos.entity.CredencialBitacora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CredencialBitacoraRepository extends JpaRepository<CredencialBitacora, Long> {
    @Query(value = "SELECT b.* FROM bitacora_credencial b " +
            "JOIN credencial_interna c ON c.tarjetaid = b.tarjetaid " +
            "WHERE b.tarjetaid = :idCredencial " +
            "AND c.clienteid = :clienteId " +
            "AND c.consignatarioid = :consignatarioId " +
            "ORDER BY b.fecha_operacion DESC",
            nativeQuery = true)
    List<CredencialBitacora> findByIdCredencialAndTenantOrderByFechaOperacionDesc(
            @Param("idCredencial") Long idCredencial,
            @Param("clienteId") Long clienteId,
            @Param("consignatarioId") Long consignatarioId
    );
}
