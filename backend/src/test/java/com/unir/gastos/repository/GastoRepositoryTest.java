package com.unir.gastos.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.unir.gastos.entity.Categoria;
import com.unir.gastos.entity.Gasto;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:file:./build/test-data/gastos")
@Transactional
class GastoRepositoryTest {

    private static final YearMonth MES = YearMonth.now().minusMonths(2);

    @Autowired
    private GastoRepository gastoRepository;

    @BeforeEach
    void setUp() {
        gastoRepository.deleteAll();
    }

    private void guardar(LocalDate fecha, String importe, Categoria categoria) {
        gastoRepository.save(new Gasto(fecha, new BigDecimal(importe), categoria, null));
    }

    @Test
    void totalesPorCategoriaAgrupaSoloLosGastosDelRangoIncluyendoLimites() {
        // Arrange
        var inicio = MES.atDay(1);
        var fin = MES.atEndOfMonth();
        guardar(inicio, "10.00", Categoria.ALIMENTACION);
        guardar(fin, "5.50", Categoria.ALIMENTACION);
        guardar(inicio.plusDays(3), "20.00", Categoria.OCIO);
        guardar(inicio.minusDays(1), "100.00", Categoria.ALIMENTACION);
        guardar(fin.plusDays(1), "100.00", Categoria.OCIO);

        // Act
        var totales = gastoRepository.totalesPorCategoria(inicio, fin);

        // Assert
        assertThat(totales)
                .usingRecursiveFieldByFieldElementComparator()
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(
                        new TotalCategoria(Categoria.ALIMENTACION, new BigDecimal("15.50"), 2L),
                        new TotalCategoria(Categoria.OCIO, new BigDecimal("20.00"), 1L));
    }

    @Test
    void totalesPorCategoriaDevuelveListaVaciaSiNoHayGastos() {
        // Act
        var totales = gastoRepository.totalesPorCategoria(MES.atDay(1), MES.atEndOfMonth());

        // Assert
        assertThat(totales).isEmpty();
    }
}
