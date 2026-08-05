package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.SuscripcionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SuscripcionUsuarioRepository extends JpaRepository<SuscripcionUsuario, Long> {

    Optional<SuscripcionUsuario> findFirstByUsuario_IdAndEstadoSuscripcionInOrderByFechaCreacionDesc(
            Long idUsuario,
            List<String> estados
    );

    Optional<SuscripcionUsuario> findFirstByUsuario_IdOrderByFechaCreacionDesc(Long idUsuario);

    Optional<SuscripcionUsuario> findFirstByCulqiSubscriptionIdOrderByFechaCreacionDesc(String culqiSubscriptionId);

    List<SuscripcionUsuario> findByUsuario_IdOrderByFechaCreacionDesc(Long idUsuario);

    boolean existsByUsuario_IdAndEstadoSuscripcionIn(Long idUsuario, List<String> estados);
    @Query("""
           SELECT su
           FROM SuscripcionUsuario su
           JOIN FETCH su.usuario u
           JOIN FETCH su.plan p
           WHERE su.usuario.id IN :idsUsuarios
             AND su.estadoSuscripcion = 'ACTIVA'
           ORDER BY su.fechaCreacion DESC
           """)
    List<SuscripcionUsuario> findActivasByUsuarioIds(
            @Param("idsUsuarios") List<Long> idsUsuarios
    );

}
