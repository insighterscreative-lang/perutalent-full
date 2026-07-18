package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Provincia;

public interface ProvinciaRepository extends JpaRepository<Provincia, Long> {

    List<Provincia> findByDepartamentoId(Long departamentoId);

}
