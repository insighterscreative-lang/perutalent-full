package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.SimpleDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoHabilidad;

public interface EmpleadoHabilidadRepository extends JpaRepository<EmpleadoHabilidad, Long> {

    List<EmpleadoHabilidad> findByIdEmpleadoId(Long idEmpleado);

    List<EmpleadoHabilidad> findByIdEmpleadoIdIn(List<Long> idsEmpleados);

    @Query("""
           SELECT DISTINCT new com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.SimpleDTO(c.id, c.nombreHabilidad)
           FROM EmpleadoHabilidad eh
           JOIN eh.idHabilidad c
           WHERE eh.idEmpleado.id IN (
               SELECT p.idEmpleado.id
               FROM Postulacion p
               WHERE p.idOferta.id = :idOferta
           )
           ORDER BY c.nombreHabilidad
           """)
    List<SimpleDTO> findOpcionesByOfertaId(@Param("idOferta") Long idOferta);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM EmpleadoHabilidad eh WHERE eh.idEmpleado.id = :idEmpleado")
    void deleteByEmpleadoId(Long idEmpleado);
}
