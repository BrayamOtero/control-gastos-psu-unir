package com.unir.gastos.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class GastoTest {

    @Test
    void constructorAndGettersStoreValues() {
        // Arrange
        var fecha = LocalDate.now().minusDays(1);
        var importe = new BigDecimal("12.50");
        var categoria = Categoria.ALIMENTACION;
        var descripcion = "Compra semanal";

        // Act
        var gasto = new Gasto(fecha, importe, categoria, descripcion);

        // Assert
        assertThat(gasto.getId()).isNull();
        assertThat(gasto.getFecha()).isEqualTo(fecha);
        assertThat(gasto.getImporte()).isEqualTo(importe);
        assertThat(gasto.getCategoria()).isEqualTo(categoria);
        assertThat(gasto.getDescripcion()).isEqualTo(descripcion);
    }

    @Test
    void jpaConstructorInitializesEmptyEntity() {
        // Arrange

        // Act
        var gasto = new Gasto();

        // Assert
        assertThat(gasto.getId()).isNull();
        assertThat(gasto.getFecha()).isNull();
        assertThat(gasto.getImporte()).isNull();
        assertThat(gasto.getCategoria()).isNull();
        assertThat(gasto.getDescripcion()).isNull();
    }

    @Test
    void settersAcceptValidValues() {
        // Arrange
        var gasto = gastoValido();
        var fecha = LocalDate.now().minusDays(2);
        var importe = new BigDecimal("99.99");
        var descripcion = "x".repeat(255);

        // Act
        gasto.setFecha(fecha);
        gasto.setImporte(importe);
        gasto.setCategoria(Categoria.TRANSPORTE);
        gasto.setDescripcion(descripcion);

        // Assert
        assertThat(gasto.getFecha()).isEqualTo(fecha);
        assertThat(gasto.getImporte()).isEqualTo(importe);
        assertThat(gasto.getCategoria()).isEqualTo(Categoria.TRANSPORTE);
        assertThat(gasto.getDescripcion()).isEqualTo(descripcion);
    }

    @Test
    void constructorRejectsInvalidValues() {
        // Arrange
        var fechaValida = LocalDate.now();
        var importeValido = new BigDecimal("1.00");
        var categoriaValida = Categoria.OCIO;

        // Act and Assert
        assertThatIllegalArgumentException().isThrownBy(() ->
                new Gasto(null, importeValido, categoriaValida, null));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new Gasto(fechaValida.plusDays(1), importeValido, categoriaValida, null));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new Gasto(fechaValida, null, categoriaValida, null));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new Gasto(fechaValida, BigDecimal.ZERO, categoriaValida, null));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new Gasto(fechaValida, new BigDecimal("1.001"), categoriaValida, null));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new Gasto(fechaValida, importeValido, null, null));
        assertThatIllegalArgumentException().isThrownBy(() ->
                new Gasto(fechaValida, importeValido, categoriaValida, "x".repeat(256)));
    }

    @Test
    void settersRejectInvalidValues() {
        // Arrange
        var gasto = gastoValido();

        // Act and Assert
        assertThatIllegalArgumentException().isThrownBy(() -> gasto.setFecha(null));
        assertThatIllegalArgumentException().isThrownBy(() -> gasto.setFecha(LocalDate.now().plusDays(1)));
        assertThatIllegalArgumentException().isThrownBy(() -> gasto.setImporte(null));
        assertThatIllegalArgumentException().isThrownBy(() -> gasto.setImporte(BigDecimal.ZERO));
        assertThatIllegalArgumentException().isThrownBy(() -> gasto.setImporte(new BigDecimal("1.001")));
        assertThatIllegalArgumentException().isThrownBy(() -> gasto.setCategoria(null));
        assertThatIllegalArgumentException().isThrownBy(() -> gasto.setDescripcion("x".repeat(256)));
    }

    private Gasto gastoValido() {
        return new Gasto(LocalDate.now(), new BigDecimal("1.00"), Categoria.OCIO, null);
    }
}