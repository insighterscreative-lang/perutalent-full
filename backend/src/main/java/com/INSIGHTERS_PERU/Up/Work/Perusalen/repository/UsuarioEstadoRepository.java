package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEstado;

public interface UsuarioEstadoRepository extends JpaRepository<UsuarioEstado, Long> {
    Optional<UsuarioEstado> findByNombreEstado(String nombreEstado);
}
