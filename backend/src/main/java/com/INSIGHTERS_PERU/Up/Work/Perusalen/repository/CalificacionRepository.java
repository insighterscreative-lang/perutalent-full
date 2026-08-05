package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    @Modifying
    @Query("""
           DELETE FROM Calificacion c
           WHERE c.idPostulacion.idOferta.id = :idOferta
           """)
    void deleteByOfertaId(@Param("idOferta") Long idOferta);
}
