package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Reclamo;

public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {
    boolean existsByCodigoReclamo(String codigoReclamo);
}
