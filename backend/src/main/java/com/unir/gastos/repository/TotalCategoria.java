package com.unir.gastos.repository;

import java.math.BigDecimal;

import com.unir.gastos.entity.Categoria;

public record TotalCategoria(Categoria categoria, BigDecimal total, Long numeroGastos) {
}
