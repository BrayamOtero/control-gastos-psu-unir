package com.unir.gastos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unir.gastos.dto.ResumenCategoriaDto;
import com.unir.gastos.dto.ResumenMensualDto;
import com.unir.gastos.entity.Categoria;
import com.unir.gastos.exception.DatosInvalidosException;
import com.unir.gastos.repository.GastoRepository;
import com.unir.gastos.repository.TotalCategoria;

@Service
@Transactional(readOnly = true)
public class ResumenMensualServiceImpl implements ResumenMensualService {

    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final GastoRepository gastoRepository;

    public ResumenMensualServiceImpl(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    @Override
    public ResumenMensualDto obtener(int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new DatosInvalidosException("El mes debe estar entre 1 y 12");
        }
        if (anio < 1 || anio > 9999) {
            throw new DatosInvalidosException("El anio debe estar entre 1 y 9999");
        }
        var periodo = YearMonth.of(anio, mes);

        var totales = new EnumMap<Categoria, TotalCategoria>(Categoria.class);
        Arrays.stream(Categoria.values()).forEach(c -> totales.put(c, new TotalCategoria(c, CERO, 0L)));
        gastoRepository.totalesPorCategoria(periodo.atDay(1), periodo.atEndOfMonth())
                .forEach(t -> totales.put(t.categoria(), t));

        var totalGastado = totales.values().stream()
                .map(TotalCategoria::total)
                .reduce(CERO, BigDecimal::add);
        var numeroGastos = totales.values().stream()
                .mapToLong(TotalCategoria::numeroGastos)
                .sum();
        // El orden estable conserva el orden del enum entre categorías con el mismo importe.
        var categorias = totales.values().stream()
                .sorted(Comparator.comparing(TotalCategoria::total).reversed())
                .map(t -> new ResumenCategoriaDto(
                        t.categoria(), t.total(), t.numeroGastos(), porcentaje(t.total(), totalGastado)))
                .toList();

        return new ResumenMensualDto(anio, mes, totalGastado, numeroGastos, categorias);
    }

    private BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return CERO;
        }
        return parte.multiply(CIEN).divide(total, 2, RoundingMode.HALF_UP);
    }
}
