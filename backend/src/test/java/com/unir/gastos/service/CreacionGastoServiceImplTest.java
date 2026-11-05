package com.unir.gastos.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.unir.gastos.dto.GastoRequestDto;
import com.unir.gastos.entity.Categoria;
import com.unir.gastos.exception.DatosInvalidosException;
import com.unir.gastos.repository.GastoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:file:./build/test-data/gastos")
@Transactional
class CreacionGastoServiceImplTest {

    @Autowired
    private CreacionGastoService creacionGastoService;

    @Autowired
    private GastoRepository gastoRepository;

    @Test
    void crearGeneraIdYGuardaElGasto() {
        // Arrange
        var request = new GastoRequestDto(
                LocalDate.now(), new BigDecimal("25.40"), Categoria.SALUD, "Farmacia");

        // Act
        var respuesta = creacionGastoService.crear(request);

        // Assert
        assertThat(respuesta.id()).isNotNull();
        assertThat(respuesta.fecha()).isEqualTo(request.fecha());
        assertThat(respuesta.importe()).isEqualByComparingTo("25.40");
        assertThat(respuesta.categoria()).isEqualTo(Categoria.SALUD);
        assertThat(respuesta.descripcion()).isEqualTo("Farmacia");
        assertThat(gastoRepository.findById(respuesta.id())).isPresent();
    }

    @Test
    void crearRechazaDatosInvalidos() {
        // Arrange
        var hoy = LocalDate.now();
        var importe = new BigDecimal("1.00");
        var invalidos = new GastoRequestDto[] {
                new GastoRequestDto(null, importe, Categoria.OCIO, null),
                new GastoRequestDto(hoy.plusDays(1), importe, Categoria.OCIO, null),
                new GastoRequestDto(hoy, null, Categoria.OCIO, null),
                new GastoRequestDto(hoy, BigDecimal.ZERO, Categoria.OCIO, null),
                new GastoRequestDto(hoy, new BigDecimal("1.001"), Categoria.OCIO, null),
                new GastoRequestDto(hoy, importe, null, null),
                new GastoRequestDto(hoy, importe, Categoria.OCIO, "x".repeat(256)),
                null
        };
        var totalAntes = gastoRepository.count();

        // Act and Assert
        for (var request : invalidos) {
            assertThatThrownBy(() -> creacionGastoService.crear(request))
                    .isInstanceOf(DatosInvalidosException.class);
        }
        assertThat(gastoRepository.count()).isEqualTo(totalAntes);
    }
}
