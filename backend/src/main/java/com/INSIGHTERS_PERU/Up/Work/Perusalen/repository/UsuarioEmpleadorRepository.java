package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleador;

public interface UsuarioEmpleadorRepository extends JpaRepository<UsuarioEmpleador, Long> {

    Optional<UsuarioEmpleador> findByUsuarioId(Long idUsuario);

    boolean existsByUsuarioId(Long idUsuario);

    boolean existsByNumDoc(String numDoc);
}
