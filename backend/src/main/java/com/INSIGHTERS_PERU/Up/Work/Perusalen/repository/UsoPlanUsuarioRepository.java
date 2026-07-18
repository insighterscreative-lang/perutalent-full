package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsoPlanUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsoPlanUsuarioRepository extends JpaRepository<UsoPlanUsuario, Long> {

    Optional<UsoPlanUsuario> findByUsuario_IdAndPeriodo(Long idUsuario, String periodo);

    boolean existsByUsuario_IdAndPeriodo(Long idUsuario, String periodo);
}