package com.efectivale.centrocostos.repository;

import com.efectivale.centrocostos.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    List<Grupo> findByActivoTrue();
    
    /**
     * Busca grupos activos filtrados por clienteId y consignatarioId para multi-tenancy.
     */
    List<Grupo> findByActivoTrueAndClienteIdAndConsignatarioId(Long clienteId, Long consignatarioId);
    
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdGrupoNot(String nombre, Long idGrupo);
    Optional<Grupo> findByNombre(String nombre);
}
