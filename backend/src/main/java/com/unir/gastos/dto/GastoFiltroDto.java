package com.unir.gastos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.unir.gastos.entity.Categoria;

public record GastoFiltroDto(
        @DateTimeFormat(iso = ISO.DATE) LocalDate fechaDesde,
        @DateTimeFormat(iso = ISO.DATE) LocalDate fechaHasta,
        BigDecimal importeMinimo,
        BigDecimal importeMaximo,
        Categoria categoria,
        String descripcion) {

    public static GastoFiltroDto sinFiltros() {
        return new GastoFiltroDto(null, null, null, null, null, null);
    }
}
