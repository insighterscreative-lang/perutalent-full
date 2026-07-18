package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.EmpleadorCategoria;

public interface EmpleadorCategoriaRepository extends JpaRepository<EmpleadorCategoria, Long> {
    List<EmpleadorCategoria> findByEmpleadorId(Long idEmpleador);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM EmpleadorCategoria ec WHERE ec.empleador.id = :idEmpleador")
    void deleteByEmpleadorId(Long idEmpleador);
}
