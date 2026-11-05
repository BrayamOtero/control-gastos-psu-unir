package com.unir.gastos.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenMensualDto(
        int anio,
        int mes,
        BigDecimal totalGastado,
        long numeroGastos,
        List<ResumenCategoriaDto> categorias) {
}
