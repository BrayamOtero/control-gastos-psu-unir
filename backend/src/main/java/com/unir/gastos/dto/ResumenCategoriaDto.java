package com.unir.gastos.dto;

import java.math.BigDecimal;

import com.unir.gastos.entity.Categoria;

public record ResumenCategoriaDto(
        Categoria categoria,
        BigDecimal total,
        long numeroGastos,
        BigDecimal porcentaje) {
}
