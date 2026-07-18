package com.INSIGHTERS_PERU.Up.Work.Perusalen.specification;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Departamento;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Distrito;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Provincia;

public class OfertaSpecification {

    public static Specification<OfertaLaboral> estadoActiva() {
        return (root, query, cb) ->
                cb.equal(root.get("estadoOferta"), "ABIERTA");
    }

    public static Specification<OfertaLaboral> conCategorias(List<Long> categorias) {
        return (root, query, cb) ->
                root.get("idCategoria").get("id").in(categorias);
    }

    public static Specification<OfertaLaboral> conModalidades(List<Long> modalidades) {
        return (root, query, cb) ->
                root.get("idMod").get("id").in(modalidades);
    }

    public static Specification<OfertaLaboral> conExperiencia(List<Long> experiencias) {
        return (root, query, cb) ->
                root.get("idExperienciaRequerida").get("id").in(experiencias);
    }

    public static Specification<OfertaLaboral> conSalario(BigDecimal min, BigDecimal max) {
        return (root, query, cb) ->
                cb.between(root.get("montoTotal"), min, max);
    }

    public static Specification<OfertaLaboral> conKeyword(String keyword) {
        return (root, query, cb) -> {
            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("titulo")), like),
                    cb.like(cb.lower(root.get("descripcion")), like)
            );
        };
    }

    public static Specification<OfertaLaboral> porUbicacion(String ubicacion) {

    return (root, query, cb) -> {

        if (ubicacion == null || ubicacion.isEmpty()) {
            return null;
        }

        String like = "%" + ubicacion.toLowerCase() + "%";

        Join<OfertaLaboral, Distrito> distrito = root.join("idDistrito");

        Join<Distrito, Provincia> provincia = distrito.join("provincia");

        Join<Provincia, Departamento> departamento = provincia.join("departamento");

        return cb.or(
                cb.like(cb.lower(distrito.get("nombreDistrito")), like),
                cb.like(cb.lower(provincia.get("nombreProvincia")), like),
                cb.like(cb.lower(departamento.get("nombreDepartamento")), like)
        );
    };
}
}
