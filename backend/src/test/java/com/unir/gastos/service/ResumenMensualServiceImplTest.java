package com.unir.gastos.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.unir.gastos.dto.ResumenCategoriaDto;
import com.unir.gastos.dto.ResumenMensualDto;
import com.unir.gastos.entity.Categoria;
import com.unir.gastos.entity.Gasto;
import com.unir.gastos.exception.DatosInvalidosException;
import com.unir.gastos.repository.GastoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:file:./build/test-data/gastos")
@Transactional
class ResumenMensualServiceImplTest {

    private static final YearMonth MES = YearMonth.now().minusMonths(2);
    private static final BigDecimal CERO = new BigDecimal("0.00");

    @Autowired
    private ResumenMensualService resumenMensualService;

    @Autowired
    private GastoRepository gastoRepository;

    @BeforeEach
    void setUp() {
        gastoRepository.deleteAll();
    }

    private void guardar(LocalDate fecha, String importe, Categoria categoria) {
        gastoRepository.save(new Gasto(fecha, new BigDecimal(importe), categoria, null));
    }

    private static ResumenCategoriaDto vacia(Categoria categoria) {
        return new ResumenCategoriaDto(categoria, CERO, 0, CERO);
    }

    @Test
    void obtenerCalculaTotalesPorcentajesYOrdenaPorImporte() {
        // Arrange
        guardar(MES.atDay(1), "10.00", Categoria.ALIMENTACION);
        guardar(MES.atEndOfMonth(), "20.00", Categoria.ALIMENTACION);
        guardar(MES.atDay(15), "10.00", Categoria.OCIO);
        guardar(MES.atDay(1).minusDays(1), "500.00", Categoria.VIVIENDA);

        // Act
        var resumen = resumenMensualService.obtener(MES.getYear(), MES.getMonthValue());

        // Assert
        assertThat(resumen.anio()).isEqualTo(MES.getYear());
        assertThat(resumen.mes()).isEqualTo(MES.getMonthValue());
        assertThat(resumen.totalGastado()).isEqualTo(new BigDecimal("40.00"));
        assertThat(resumen.numeroGastos()).isEqualTo(3);
        assertThat(resumen.categorias()).containsExactly(
                new ResumenCategoriaDto(Categoria.ALIMENTACION, new BigDecimal("30.00"), 2, new BigDecimal("75.00")),
                new ResumenCategoriaDto(Categoria.OCIO, new BigDecimal("10.00"), 1, new BigDecimal("25.00")),
                vacia(Categoria.TRANSPORTE),
                vacia(Categoria.VIVIENDA),
                vacia(Categoria.SERVICIOS),
                vacia(Categoria.SALUD),
                vacia(Categoria.OTROS));
    }

    @Test
    void obtenerRedondeaPorcentajesADosDecimales() {
        // Arrange
        guardar(MES.atDay(2), "1.00", Categoria.SALUD);
        guardar(MES.atDay(2), "1.00", Categoria.OCIO);
        guardar(MES.atDay(2), "1.00", Categoria.OTROS);

        // Act
        var resumen = resumenMensualService.obtener(MES.getYear(), MES.getMonthValue());

        // Assert
        assertThat(resumen.categorias())
                .filteredOn(c -> c.numeroGastos() > 0)
                .extracting(ResumenCategoriaDto::porcentaje)
                .containsOnly(new BigDecimal("33.33"));
    }

    @Test
    void obtenerDevuelveTodoACeroSiNoHayGastosOEsUnMesFuturo() {
        // Arrange
        var futuro = YearMonth.now().plusMonths(1);

        // Act
        var vacio = resumenMensualService.obtener(MES.getYear(), MES.getMonthValue());
        var mesFuturo = resumenMensualService.obtener(futuro.getYear(), futuro.getMonthValue());

        // Assert
        for (var resumen : new ResumenMensualDto[] {vacio, mesFuturo}) {
            assertThat(resumen.totalGastado()).isEqualTo(CERO);
            assertThat(resumen.numeroGastos()).isZero();
            assertThat(resumen.categorias())
                    .containsExactly(Arrays.stream(Categoria.values())
                            .map(ResumenMensualServiceImplTest::vacia)
                            .toArray(ResumenCategoriaDto[]::new));
        }
    }

    @Test
    void obtenerRechazaMesOAnioFueraDeRango() {
        // Act and Assert
        assertThatThrownBy(() -> resumenMensualService.obtener(2026, 0)).isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> resumenMensualService.obtener(2026, 13)).isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> resumenMensualService.obtener(0, 5)).isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> resumenMensualService.obtener(10000, 5)).isInstanceOf(DatosInvalidosException.class);
    }
}
