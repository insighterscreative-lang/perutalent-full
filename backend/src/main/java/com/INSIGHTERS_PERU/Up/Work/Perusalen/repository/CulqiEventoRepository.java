package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.CulqiEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CulqiEventoRepository extends JpaRepository<CulqiEvento, Long> {

    boolean existsByCulqiEventId(String culqiEventId);

    Optional<CulqiEvento> findByCulqiEventId(String culqiEventId);
}
