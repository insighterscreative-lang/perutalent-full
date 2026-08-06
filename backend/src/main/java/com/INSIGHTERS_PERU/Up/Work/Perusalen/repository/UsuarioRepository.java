package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.id = :idUsuario")
    Optional<Usuario> findByIdForUpdate(@Param("idUsuario") Long idUsuario);
}
