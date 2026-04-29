package com.efectivale.centrocostos.repository;

import com.efectivale.centrocostos.entity.CredencialBitacora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CredencialBitacoraRepository extends JpaRepository<CredencialBitacora, Long> {
    List<CredencialBitacora> findByIdCredencialOrderByFechaOperacionDesc(Long idCredencial);
}
