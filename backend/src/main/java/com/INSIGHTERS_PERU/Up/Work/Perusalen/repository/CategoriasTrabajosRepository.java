package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.CategoriasTrabajos;

public interface CategoriasTrabajosRepository extends JpaRepository<CategoriasTrabajos, Long> {
    List<CategoriasTrabajos> findAll();
}
