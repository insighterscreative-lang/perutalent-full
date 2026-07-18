package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.PlanSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanSuscripcionRepository extends JpaRepository<PlanSuscripcion, Long> {

    Optional<PlanSuscripcion> findByNombrePlan(String nombrePlan);

    Optional<PlanSuscripcion> findByIdAndActivoTrue(Long id);

    List<PlanSuscripcion> findByActivoTrueOrderByPrecioCentimosAsc();
}