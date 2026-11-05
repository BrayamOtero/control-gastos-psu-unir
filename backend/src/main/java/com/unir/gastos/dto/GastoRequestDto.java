package com.unir.gastos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.unir.gastos.entity.Categoria;

public record GastoRequestDto(
        LocalDate fecha,
        BigDecimal importe,
        Categoria categoria,
        String descripcion) {
}
