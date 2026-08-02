package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.PagoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoSuscripcionRepository extends JpaRepository<PagoSuscripcion, Long> {

    List<PagoSuscripcion> findByUsuario_IdOrderByFechaPagoDesc(Long idUsuario);

    Optional<PagoSuscripcion> findByCulqiChargeId(String culqiChargeId);

    boolean existsByCulqiChargeId(String culqiChargeId);
}
