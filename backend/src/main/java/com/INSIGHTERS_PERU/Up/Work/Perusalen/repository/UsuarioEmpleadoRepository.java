package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.UsuarioEmpleado;

public interface UsuarioEmpleadoRepository extends JpaRepository<UsuarioEmpleado, Long> {

    Optional<UsuarioEmpleado> findByUsuarioId(Long usuarioId);

    boolean existsByNumDoc(String numDoc);
}
