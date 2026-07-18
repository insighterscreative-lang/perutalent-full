package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoHabilidad;

public interface EmpleadoHabilidadRepository extends JpaRepository<EmpleadoHabilidad, Long> {

    List<EmpleadoHabilidad> findByIdEmpleadoId(Long idEmpleado);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM EmpleadoHabilidad eh WHERE eh.idEmpleado.id = :idEmpleado")
    void deleteByEmpleadoId(Long idEmpleado);

}
