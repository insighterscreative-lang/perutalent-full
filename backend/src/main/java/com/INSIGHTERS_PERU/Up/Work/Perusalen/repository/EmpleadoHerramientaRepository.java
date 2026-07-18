package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoHerramienta;

public interface EmpleadoHerramientaRepository extends JpaRepository<EmpleadoHerramienta, Long> {

    List<EmpleadoHerramienta> findByIdEmpleadoId(Long idEmpleado);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM EmpleadoHerramienta eh WHERE eh.idEmpleado.id = :idEmpleado")
    void deleteByEmpleadoId(Long idEmpleado);

}
