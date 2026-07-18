package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaHabilidad;

public interface OfertaHabilidadRepository extends JpaRepository<OfertaHabilidad, Long> {

    @Modifying
    @Query("""
           DELETE FROM OfertaHabilidad oh
           WHERE oh.idOferta.id = :idOferta
           """)
    void deleteByOfertaId(@Param("idOferta") Long idOferta);
}