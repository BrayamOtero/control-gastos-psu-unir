package com.unir.gastos.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.unir.gastos.dto.GastoRequestDto;
import com.unir.gastos.dto.GastoResponseDto;
import com.unir.gastos.entity.Categoria;
import com.unir.gastos.entity.Gasto;
import com.unir.gastos.exception.DatosInvalidosException;
import com.unir.gastos.exception.GastoNoEncontradoException;
import com.unir.gastos.repository.GastoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:file:./build/test-data/gastos")
@Transactional
class ModificacionGastoServiceImplTest {

    @Autowired
    private ModificacionGastoService modificacionGastoService;

    @Autowired
    private GastoRepository gastoRepository;

    private Gasto existente;

    @BeforeEach
    void setUp() {
        existente = gastoRepository.save(new Gasto(
                LocalDate.now().minusDays(3), new BigDecimal("10.00"), Categoria.OCIO, "Cine"));
    }

    @Test
    void modificarReemplazaTodosLosCamposYConservaElId() {
        // Arrange
        var id = existente.getId();
        var request = new GastoRequestDto(
                LocalDate.now(), new BigDecimal("80.25"), Categoria.VIVIENDA, null);

        // Act
        var respuesta = modificacionGastoService.modificar(id, request);

        // Assert
        var esperado = new GastoResponseDto(id, request.fecha(), request.importe(), Categoria.VIVIENDA, null);
        assertThat(respuesta).isEqualTo(esperado);
        assertThat(GastoResponseDto.desde(gastoRepository.findById(id).orElseThrow())).isEqualTo(esperado);
    }

    @Test
    void modificarLanzaExcepcionSiNoExiste() {
        // Arrange
        Long idInexistente = existente.getId() + 1000;
        var request = new GastoRequestDto(LocalDate.now(), BigDecimal.ONE, Categoria.OTROS, null);

        // Act and Assert
        assertThatThrownBy(() -> modificacionGastoService.modificar(idInexistente, request))
                .isInstanceOf(GastoNoEncontradoException.class);
    }

    @Test
    void modificarRechazaIdNoPositivo() {
        // Arrange
        var request = new GastoRequestDto(LocalDate.now(), BigDecimal.ONE, Categoria.OTROS, null);

        // Act and Assert
        assertThatThrownBy(() -> modificacionGastoService.modificar(null, request))
                .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> modificacionGastoService.modificar(0L, request))
                .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> modificacionGastoService.modificar(-1L, request))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void modificarRechazaDatosInvalidosSinAlterarElGasto() {
        // Arrange
        var id = existente.getId();
        var hoy = LocalDate.now();
        var importe = new BigDecimal("1.00");
        var invalidos = new GastoRequestDto[] {
                new GastoRequestDto(null, importe, Categoria.SALUD, null),
                new GastoRequestDto(hoy.plusDays(1), importe, Categoria.SALUD, null),
                new GastoRequestDto(hoy, null, Categoria.SALUD, null),
                new GastoRequestDto(hoy, new BigDecimal("-5.00"), Categoria.SALUD, null),
                new GastoRequestDto(hoy, new BigDecimal("1.001"), Categoria.SALUD, null),
                new GastoRequestDto(hoy, importe, null, null),
                new GastoRequestDto(hoy, importe, Categoria.SALUD, "x".repeat(256)),
                null
        };

        // Act and Assert
        for (var request : invalidos) {
            assertThatThrownBy(() -> modificacionGastoService.modificar(id, request))
                    .isInstanceOf(DatosInvalidosException.class);
        }
        var actual = gastoRepository.findById(id).orElseThrow();
        assertThat(actual.getImporte()).isEqualByComparingTo("10.00");
        assertThat(actual.getCategoria()).isEqualTo(Categoria.OCIO);
        assertThat(actual.getDescripcion()).isEqualTo("Cine");
    }
}
