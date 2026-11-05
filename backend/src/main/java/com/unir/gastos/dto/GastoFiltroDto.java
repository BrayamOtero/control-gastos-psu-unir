package com.unir.gastos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.unir.gastos.entity.Categoria;

public record GastoFiltroDto(
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        BigDecimal importeMinimo,
        BigDecimal importeMaximo,
        Categoria categoria,
        String descripcion) {

    public static GastoFiltroDto sinFiltros() {
        return new GastoFiltroDto(null, null, null, null, null, null);
    }
}
