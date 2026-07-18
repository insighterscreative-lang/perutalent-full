package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.SuscripcionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuscripcionUsuarioRepository extends JpaRepository<SuscripcionUsuario, Long> {

    Optional<SuscripcionUsuario> findFirstByUsuario_IdAndEstadoSuscripcionInOrderByFechaCreacionDesc(
            Long idUsuario,
            List<String> estados
    );

    Optional<SuscripcionUsuario> findFirstByUsuario_IdOrderByFechaCreacionDesc(Long idUsuario);

    List<SuscripcionUsuario> findByUsuario_IdOrderByFechaCreacionDesc(Long idUsuario);

    boolean existsByUsuario_IdAndEstadoSuscripcionIn(Long idUsuario, List<String> estados);
}