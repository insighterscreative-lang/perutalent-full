package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoModalidad;

public interface EmpleadoModalidadRepository extends JpaRepository<EmpleadoModalidad, Long> {

    List<EmpleadoModalidad> findByIdEmpleadoId(Long idEmpleado);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM EmpleadoModalidad em WHERE em.idEmpleado.id = :idEmpleado")
    void deleteByEmpleadoId(Long idEmpleado);

}
