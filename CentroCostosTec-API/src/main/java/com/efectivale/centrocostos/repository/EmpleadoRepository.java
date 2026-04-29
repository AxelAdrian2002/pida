package com.efectivale.centrocostos.repository;

import com.efectivale.centrocostos.entity.Empleado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findByNumeroEmpleado(String numeroEmpleado);
    boolean existsByNumeroEmpleado(String numeroEmpleado);

    /**
     * Busca empleado por número, cliente y consignatario para multi-tenancy.
     */
    Optional<Empleado> findByNumeroEmpleadoAndClienteIdAndConsignatarioId(
        String numeroEmpleado, Long clienteId, Long consignatarioId
    );

    @Query(
        value = "SELECT * FROM tmemp e " +
                "WHERE (e.tbist IS NULL OR e.tbist <> 'X') " +
                "AND (:nombre IS NULL OR :nombre = '' OR LOWER(e.tnoem) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
                "AND (:departamento IS NULL OR :departamento = '' OR e.tnuec = :departamento)",
        countQuery = "SELECT COUNT(*) FROM tmemp e " +
                "WHERE (e.tbist IS NULL OR e.tbist <> 'X') " +
                "AND (:nombre IS NULL OR :nombre = '' OR LOWER(e.tnoem) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
                "AND (:departamento IS NULL OR :departamento = '' OR e.tnuec = :departamento)",
        nativeQuery = true
    )
    Page<Empleado> buscarActivos(@Param("nombre") String nombre,
                                  @Param("departamento") String departamento,
                                  Pageable pageable);

    /**
     * Busca empleados activos filtrados por cliente y consignatario para multi-tenancy.
     */
    @Query(
        value = "SELECT * FROM tmemp e " +
                "WHERE (e.tbist IS NULL OR e.tbist <> 'X') " +
                "AND (:nombre IS NULL OR :nombre = '' OR LOWER(e.tnoem) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
                "AND (:departamento IS NULL OR :departamento = '' OR e.tnuec = :departamento) " +
                "AND e.tnucl = :clienteId AND e.tnuco = :consignatarioId",
        countQuery = "SELECT COUNT(*) FROM tmemp e " +
                "WHERE (e.tbist IS NULL OR e.tbist <> 'X') " +
                "AND (:nombre IS NULL OR :nombre = '' OR LOWER(e.tnoem) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
                "AND (:departamento IS NULL OR :departamento = '' OR e.tnuec = :departamento) " +
                "AND e.tnucl = :clienteId AND e.tnuco = :consignatarioId",
        nativeQuery = true
    )
    Page<Empleado> buscarActivos(@Param("nombre") String nombre,
                                  @Param("departamento") String departamento,
                                  @Param("clienteId") Long clienteId,
                                  @Param("consignatarioId") Long consignatarioId,
                                  Pageable pageable);
}
