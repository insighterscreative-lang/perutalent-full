package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.SimpleDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Postulacion;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    boolean existsByIdOfertaIdAndIdEmpleadoId(Long idOferta, Long idEmpleado);

    List<Postulacion> findByIdOfertaId(Long idOferta);

    List<Postulacion> findByIdEmpleadoId(Long idEmpleado);

    @EntityGraph(attributePaths = {
            "idOferta",
            "idOferta.idEmpleador",
            "idOferta.idDistrito",
            "idOferta.idCategoria",
            "idOferta.idMod",
            "idOferta.idExperienciaRequerida",
            "idOferta.idDuracion"
    })
    Page<Postulacion> findByIdEmpleadoId(Long idEmpleado, Pageable pageable);

    List<Postulacion> findByIdOfertaIdOrderByFechaPostulacionDesc(Long idOferta);

    @Query(value = """
            SELECT p.id_postulacion
            FROM postulacion p
            JOIN usuario_empleado ue ON ue.id_empleado = p.id_empleado
            JOIN usuario u ON u.id_usuario = ue.id_usuario
            WHERE p.id_oferta = :idOferta
              AND (:estado = '' OR p.estado_postulacion = :estado)
              AND (
                    :texto = ''
                    OR LOWER(CONCAT(COALESCE(ue.nombre, ''), ' ', COALESCE(ue.apellido, ''), ' ', COALESCE(u.email, '')))
                       LIKE LOWER(CONCAT('%', :texto, '%'))
                  )
              AND (:distritoId = 0 OR ue.id_distrito = :distritoId)
              AND (
                    :modalidadId = 0
                    OR EXISTS (
                        SELECT 1
                        FROM empleado_modalidad em
                        WHERE em.id_empleado = ue.id_empleado
                          AND em.id_mod = :modalidadId
                    )
                  )
              AND (
                    :habilidadId = 0
                    OR EXISTS (
                        SELECT 1
                        FROM empleado_habilidad eh
                        WHERE eh.id_empleado = ue.id_empleado
                          AND eh.id_habilidad = :habilidadId
                    )
                  )
              AND (
                    :herramientaId = 0
                    OR EXISTS (
                        SELECT 1
                        FROM empleado_herramienta eht
                        WHERE eht.id_empleado = ue.id_empleado
                          AND eht.id_herramienta = :herramientaId
                    )
                  )
            ORDER BY
              CASE WHEN EXISTS (
                  SELECT 1
                  FROM suscripcion_usuario su
                  JOIN plan_suscripcion ps ON ps.id_plan = su.id_plan
                  WHERE su.id_usuario = u.id_usuario
                    AND su.estado_suscripcion = 'ACTIVA'
                    AND su.fecha_inicio <= CURRENT_DATE
                    AND (su.fecha_fin IS NULL OR su.fecha_fin >= CURRENT_DATE)
                    AND ps.prioridad_postulante = TRUE
              ) THEN 1 ELSE 0 END DESC,
              p.fecha_postulacion DESC,
              p.id_postulacion DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM postulacion p
            JOIN usuario_empleado ue ON ue.id_empleado = p.id_empleado
            JOIN usuario u ON u.id_usuario = ue.id_usuario
            WHERE p.id_oferta = :idOferta
              AND (:estado = '' OR p.estado_postulacion = :estado)
              AND (
                    :texto = ''
                    OR LOWER(CONCAT(COALESCE(ue.nombre, ''), ' ', COALESCE(ue.apellido, ''), ' ', COALESCE(u.email, '')))
                       LIKE LOWER(CONCAT('%', :texto, '%'))
                  )
              AND (:distritoId = 0 OR ue.id_distrito = :distritoId)
              AND (
                    :modalidadId = 0
                    OR EXISTS (
                        SELECT 1
                        FROM empleado_modalidad em
                        WHERE em.id_empleado = ue.id_empleado
                          AND em.id_mod = :modalidadId
                    )
                  )
              AND (
                    :habilidadId = 0
                    OR EXISTS (
                        SELECT 1
                        FROM empleado_habilidad eh
                        WHERE eh.id_empleado = ue.id_empleado
                          AND eh.id_habilidad = :habilidadId
                    )
                  )
              AND (
                    :herramientaId = 0
                    OR EXISTS (
                        SELECT 1
                        FROM empleado_herramienta eht
                        WHERE eht.id_empleado = ue.id_empleado
                          AND eht.id_herramienta = :herramientaId
                    )
                  )
            """,
            nativeQuery = true)
    Page<Long> findIdsPostulantesPaginados(
            @Param("idOferta") Long idOferta,
            @Param("estado") String estado,
            @Param("texto") String texto,
            @Param("distritoId") Long distritoId,
            @Param("modalidadId") Long modalidadId,
            @Param("habilidadId") Long habilidadId,
            @Param("herramientaId") Long herramientaId,
            Pageable pageable
    );

    @Query("""
           SELECT DISTINCT p
           FROM Postulacion p
           JOIN FETCH p.idEmpleado e
           JOIN FETCH e.usuario u
           JOIN FETCH e.distrito d
           JOIN FETCH p.idOferta o
           JOIN FETCH o.idEmpleador emp
           WHERE p.id IN :ids
           """)
    List<Postulacion> findAllByIdWithDetalle(@Param("ids") List<Long> ids);

    @Query("""
           SELECT DISTINCT new com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.SimpleDTO(d.id, d.nombreDistrito)
           FROM Postulacion p
           JOIN p.idEmpleado e
           JOIN e.distrito d
           WHERE p.idOferta.id = :idOferta
           ORDER BY d.nombreDistrito
           """)
    List<SimpleDTO> findDistritosDisponiblesByOfertaId(@Param("idOferta") Long idOferta);

    @Modifying
    @Query("""
           DELETE FROM Postulacion p
           WHERE p.idOferta.id = :idOferta
           """)
    void deleteByIdOfertaId(@Param("idOferta") Long idOferta);
}
