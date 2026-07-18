package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadoCategoria;

public interface EmpleadoCategoriaRepository extends JpaRepository<EmpleadoCategoria, Long> {

    List<EmpleadoCategoria> findByIdEmpleadoId(Long idEmpleado);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM EmpleadoCategoria ec WHERE ec.idEmpleado.id = :idEmpleado")
    void deleteByEmpleadoId(Long idEmpleado);

}
