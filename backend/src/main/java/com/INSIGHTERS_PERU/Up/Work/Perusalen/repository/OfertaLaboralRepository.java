package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;

public interface OfertaLaboralRepository extends JpaRepository<OfertaLaboral, Long>, JpaSpecificationExecutor<OfertaLaboral> {

    List<OfertaLaboral> findByEstadoOferta(String estado);

    @Query("SELECT o FROM OfertaLaboral o " +
           "LEFT JOIN FETCH o.habilidades h " +
           "LEFT JOIN FETCH h.idHabilidad " +
           "WHERE o.estadoOferta = :estado")
    List<OfertaLaboral> findActivasWithHabilidades(@Param("estado") String estado);

    @Query("SELECT o FROM OfertaLaboral o " +
           "LEFT JOIN FETCH o.habilidades h " +
           "LEFT JOIN FETCH h.idHabilidad " +
           "WHERE o.id = :id")
    Optional<OfertaLaboral> findByIdWithHabilidades(@Param("id") Long id);

    int countByEmpleadoSeleccionadoIdAndEstadoOferta(
            Long idEmpleado,
            String estadoOferta
    );

    List<OfertaLaboral> findByEmpleadoSeleccionadoIdAndEstadoOferta(
            Long idEmpleado,
            String estadoOferta
    );

    int countByEmpleadoSeleccionadoIdAndEstadoOfertaIn(
            Long idEmpleado,
            List<String> estados
    );

    List<OfertaLaboral> findByEmpleadoSeleccionadoIdAndEstadoOfertaIn(
            Long idEmpleado,
            List<String> estados
    );

    int countByIdEmpleadorIdAndEstadoOferta(
            Long idEmpleador,
            String estadoOferta
    );

    List<OfertaLaboral> findByIdEmpleadorIdAndEstadoOferta(
            Long idEmpleador,
            String estadoOferta
    );

    int countByIdEmpleadorIdAndEstadoOfertaIn(
            Long idEmpleador,
            List<String> estados
    );

    List<OfertaLaboral> findByIdEmpleadorIdAndEstadoOfertaIn(
            Long idEmpleador,
            List<String> estados
    );

    @Query("""
           SELECT DISTINCT o.idMod.nombreMod
           FROM OfertaLaboral o
           WHERE o.idEmpleador.id = :idEmpleador
           """)
    List<String> findModalidadesContratacionByEmpleadorId(
            @Param("idEmpleador") Long idEmpleador
    );

    boolean existsByCodigoInterno(String codigoInterno);
}