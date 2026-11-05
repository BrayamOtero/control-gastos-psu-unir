package com.unir.gastos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.unir.gastos.entity.Categoria;
import com.unir.gastos.entity.Gasto;

public record GastoResponseDto(
        Long id,
        LocalDate fecha,
        BigDecimal importe,
        Categoria categoria,
        String descripcion) {

    public static GastoResponseDto desde(Gasto gasto) {
        return new GastoResponseDto(
                gasto.getId(),
                gasto.getFecha(),
                gasto.getImporte(),
                gasto.getCategoria(),
                gasto.getDescripcion());
    }
}
