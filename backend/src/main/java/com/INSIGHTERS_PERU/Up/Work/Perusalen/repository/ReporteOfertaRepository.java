package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.ReporteOferta;

public interface ReporteOfertaRepository extends JpaRepository<ReporteOferta, Long> {
    boolean existsByOfertaIdAndUsuarioReportanteId(Long idOferta, Long idUsuario);
}
