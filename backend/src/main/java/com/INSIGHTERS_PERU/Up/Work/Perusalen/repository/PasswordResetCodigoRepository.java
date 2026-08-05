package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.PasswordResetCodigo;

public interface PasswordResetCodigoRepository extends JpaRepository<PasswordResetCodigo, Long> {

    @EntityGraph(attributePaths = "usuario")
    Optional<PasswordResetCodigo> findFirstByUsuarioEmailIgnoreCaseAndUsadoFalseOrderByFechaCreacionDesc(String email);

    Optional<PasswordResetCodigo> findFirstByUsuarioEmailIgnoreCaseOrderByFechaCreacionDesc(String email);

    @Query("""
           SELECT COUNT(p)
           FROM PasswordResetCodigo p
           WHERE p.usuario.id = :idUsuario
             AND p.fechaCreacion >= :desde
           """)
    long contarSolicitudesDesde(
            @Param("idUsuario") Long idUsuario,
            @Param("desde") LocalDateTime desde
    );

    @Modifying
    @Query("""
           UPDATE PasswordResetCodigo p
           SET p.usado = true,
               p.fechaUso = :fechaUso
           WHERE p.usuario.id = :idUsuario
             AND p.usado = false
           """)
    void invalidarCodigosActivos(
            @Param("idUsuario") Long idUsuario,
            @Param("fechaUso") LocalDateTime fechaUso
    );
}
