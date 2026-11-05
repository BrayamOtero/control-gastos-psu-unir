package com.unir.gastos.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.unir.gastos.entity.Categoria;
import com.unir.gastos.entity.Gasto;
import com.unir.gastos.exception.DatosInvalidosException;
import com.unir.gastos.exception.GastoNoEncontradoException;
import com.unir.gastos.repository.GastoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:file:./build/test-data/gastos")
@Transactional
class EliminacionGastoServiceImplTest {

    @Autowired
    private EliminacionGastoService eliminacionGastoService;

    @Autowired
    private GastoRepository gastoRepository;

    @Test
    void eliminarBorraElGasto() {
        // Arrange
        var id = gastoRepository.save(new Gasto(
                LocalDate.now(), new BigDecimal("5.00"), Categoria.SERVICIOS, null)).getId();

        // Act
        eliminacionGastoService.eliminar(id);

        // Assert
        assertThat(gastoRepository.findById(id)).isEmpty();
    }

    @Test
    void eliminarLanzaExcepcionSiNoExiste() {
        // Arrange
        Long idInexistente = Long.MAX_VALUE;

        // Act and Assert
        assertThatThrownBy(() -> eliminacionGastoService.eliminar(idInexistente))
                .isInstanceOf(GastoNoEncontradoException.class);
    }

    @Test
    void eliminarRechazaIdNoPositivo() {
        // Act and Assert
        assertThatThrownBy(() -> eliminacionGastoService.eliminar(null))
                .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> eliminacionGastoService.eliminar(0L))
                .isInstanceOf(DatosInvalidosException.class);
    }
}
