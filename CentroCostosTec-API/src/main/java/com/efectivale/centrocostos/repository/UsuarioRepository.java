package com.efectivale.centrocostos.repository;

import com.efectivale.centrocostos.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findFirstByUsernameOrderByActivoDescFechaUltimoAccesoDescIdUsuarioDesc(String username);
    Optional<Usuario> findFirstByUsernameAndActivoTrueOrderByFechaUltimoAccesoDescIdUsuarioDesc(String username);
    boolean existsByUsername(String username);
}
