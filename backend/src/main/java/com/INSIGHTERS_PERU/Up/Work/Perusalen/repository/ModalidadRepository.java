package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Modalidad;

public interface ModalidadRepository extends JpaRepository<Modalidad, Long> {
    List<Modalidad> findAll();
}
