package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.ReporteProblemaTecnico;

public interface ReporteProblemaTecnicoRepository extends JpaRepository<ReporteProblemaTecnico, Long> {
    boolean existsByCodigoReporte(String codigoReporte);
}
