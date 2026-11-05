package com.unir.gastos.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.unir.gastos.entity.Categoria;
import com.unir.gastos.entity.Gasto;

public final class GastoSpecifications {

    private static final char ESCAPE = '\\';

    private GastoSpecifications() {
    }

    public static Specification<Gasto> fechaDesde(LocalDate fechaDesde) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<LocalDate>get("fecha"), fechaDesde);
    }

    public static Specification<Gasto> fechaHasta(LocalDate fechaHasta) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<LocalDate>get("fecha"), fechaHasta);
    }

    public static Specification<Gasto> importeMinimo(BigDecimal importeMinimo) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<BigDecimal>get("importe"), importeMinimo);
    }

    public static Specification<Gasto> importeMaximo(BigDecimal importeMaximo) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<BigDecimal>get("importe"), importeMaximo);
    }

    public static Specification<Gasto> categoria(Categoria categoria) {
        return (root, query, cb) -> cb.equal(root.get("categoria"), categoria);
    }

    public static Specification<Gasto> descripcionContiene(String texto) {
        var patron = "%" + escaparComodines(texto.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("descripcion")), patron, ESCAPE);
    }

    // Evita que % y _ introducidos por el usuario actúen como comodines de LIKE.
    private static String escaparComodines(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
