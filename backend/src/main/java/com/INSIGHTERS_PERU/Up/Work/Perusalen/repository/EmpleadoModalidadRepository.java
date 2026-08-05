package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.SimpleDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoModalidad;

public interface EmpleadoModalidadRepository extends JpaRepository<EmpleadoModalidad, Long> {

    List<EmpleadoModalidad> findByIdEmpleadoId(Long idEmpleado);

    List<EmpleadoModalidad> findByIdEmpleadoIdIn(List<Long> idsEmpleados);

    @Query("""
           SELECT DISTINCT new com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.SimpleDTO(c.id, c.nombreMod)
           FROM EmpleadoModalidad em
           JOIN em.idMod c
           WHERE em.idEmpleado.id IN (
               SELECT p.idEmpleado.id
               FROM Postulacion p
               WHERE p.idOferta.id = :idOferta
           )
           ORDER BY c.nombreMod
           """)
    List<SimpleDTO> findOpcionesByOfertaId(@Param("idOferta") Long idOferta);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM EmpleadoModalidad em WHERE em.idEmpleado.id = :idEmpleado")
    void deleteByEmpleadoId(Long idEmpleado);
}
