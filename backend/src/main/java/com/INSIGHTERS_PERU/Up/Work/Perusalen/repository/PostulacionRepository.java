package com.INSIGHTERS_PERU.Up.Work.Perusalen.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Postulacion;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    boolean existsByIdOfertaIdAndIdEmpleadoId(Long idOferta, Long idEmpleado);

    List<Postulacion> findByIdOfertaId(Long idOferta);

    List<Postulacion> findByIdEmpleadoId(Long idEmpleado);

    List<Postulacion> findByIdOfertaIdOrderByFechaPostulacionDesc(Long idOferta);
}