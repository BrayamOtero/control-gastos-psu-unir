package com.unir.gastos.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.unir.gastos.dto.GastoFiltroDto;
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
class ConsultaGastoServiceImplTest {

    private static final LocalDate HOY = LocalDate.now();
    private static final Pageable PAGINA = PageRequest.of(0, 10, Sort.by("id"));

    @Autowired
    private ConsultaGastoService consultaGastoService;

    @Autowired
    private GastoRepository gastoRepository;

    private Gasto supermercado;
    private Gasto taxi;
    private Gasto cine;

    private static Long[] ids(Page<GastoResponseDto> pagina) {
        return pagina.getContent().stream().map(GastoResponseDto::id).toArray(Long[]::new);
    }

    private static Long[] ids(Gasto... gastos) {
        return java.util.Arrays.stream(gastos).map(Gasto::getId).toArray(Long[]::new);
    }

    @BeforeEach
    void setUp() {
        gastoRepository.deleteAll();
        supermercado = gastoRepository.save(new Gasto(
                HOY.minusDays(10), new BigDecimal("50.00"), Categoria.ALIMENTACION, "Compra Supermercado"));
        taxi = gastoRepository.save(new Gasto(
                HOY.minusDays(5), new BigDecimal("15.50"), Categoria.TRANSPORTE, "Taxi al aeropuerto"));
        cine = gastoRepository.save(new Gasto(
                HOY, new BigDecimal("9.99"), Categoria.OCIO, null));
    }

    @Test
    void obtenerPorIdDevuelveElGastoComoDto() {
        // Arrange
        var id = taxi.getId();

        // Act
        var resultado = consultaGastoService.obtenerPorId(id);

        // Assert
        assertThat(resultado).isEqualTo(new GastoResponseDto(
                id, taxi.getFecha(), taxi.getImporte(), Categoria.TRANSPORTE, "Taxi al aeropuerto"));
    }

    @Test
    void obtenerPorIdLanzaExcepcionSiNoExiste() {
        // Arrange
        Long idInexistente = cine.getId() + 1000;

        // Act and Assert
        assertThatThrownBy(() -> consultaGastoService.obtenerPorId(idInexistente))
                .isInstanceOf(GastoNoEncontradoException.class)
                .hasMessageContaining(idInexistente.toString());
    }

    @Test
    void obtenerPorIdRechazaIdNulo() {
        // Act and Assert
        assertThatThrownBy(() -> consultaGastoService.obtenerPorId(null))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void buscarSinFiltrosDevuelveTodosPaginados() {
        // Act
        var pagina = consultaGastoService.buscar(GastoFiltroDto.sinFiltros(), PAGINA);
        var paginaFiltroNulo = consultaGastoService.buscar(null, PAGINA);

        // Assert
        assertThat(ids(pagina)).containsExactly(ids(supermercado, taxi, cine));
        assertThat(paginaFiltroNulo.getTotalElements()).isEqualTo(3);
    }

    @Test
    void buscarPorRangoDeFechasInclusivo() {
        // Arrange
        var filtro = new GastoFiltroDto(HOY.minusDays(10), HOY.minusDays(5), null, null, null, null);

        // Act
        var pagina = consultaGastoService.buscar(filtro, PAGINA);

        // Assert
        assertThat(ids(pagina)).containsExactly(ids(supermercado, taxi));
    }

    @Test
    void buscarConUnSoloExtremoDeRango() {
        // Arrange
        var soloFechaHasta = new GastoFiltroDto(null, HOY.minusDays(6), null, null, null, null);
        var soloImporteMinimo = new GastoFiltroDto(null, null, new BigDecimal("15.50"), null, null, null);

        // Act
        var porFecha = consultaGastoService.buscar(soloFechaHasta, PAGINA);
        var porImporte = consultaGastoService.buscar(soloImporteMinimo, PAGINA);

        // Assert
        assertThat(ids(porFecha)).containsExactly(ids(supermercado));
        assertThat(ids(porImporte)).containsExactly(ids(supermercado, taxi));
    }

    @Test
    void buscarPorRangoDeImportesInclusivo() {
        // Arrange
        var filtro = new GastoFiltroDto(null, null, new BigDecimal("9.99"), new BigDecimal("15.50"), null, null);

        // Act
        var pagina = consultaGastoService.buscar(filtro, PAGINA);

        // Assert
        assertThat(ids(pagina)).containsExactly(ids(taxi, cine));
    }

    @Test
    void buscarPorCategoriaExacta() {
        // Arrange
        var filtro = new GastoFiltroDto(null, null, null, null, Categoria.OCIO, null);

        // Act
        var pagina = consultaGastoService.buscar(filtro, PAGINA);

        // Assert
        assertThat(ids(pagina)).containsExactly(ids(cine));
    }

    @Test
    void buscarPorDescripcionParcialIgnorandoMayusculasYEspacios() {
        // Arrange
        var filtro = new GastoFiltroDto(null, null, null, null, null, "  SUPERMER  ");

        // Act
        var pagina = consultaGastoService.buscar(filtro, PAGINA);

        // Assert
        assertThat(ids(pagina)).containsExactly(ids(supermercado));
    }

    @Test
    void buscarIgnoraDescripcionEnBlanco() {
        // Arrange
        var filtro = new GastoFiltroDto(null, null, null, null, null, "   ");

        // Act
        var pagina = consultaGastoService.buscar(filtro, PAGINA);

        // Assert
        assertThat(pagina.getTotalElements()).isEqualTo(3);
    }

    @Test
    void buscarTrataComodinesDeDescripcionComoTextoLiteral() {
        // Arrange
        var filtro = new GastoFiltroDto(null, null, null, null, null, "%");

        // Act
        var pagina = consultaGastoService.buscar(filtro, PAGINA);

        // Assert
        assertThat(pagina.getContent()).isEmpty();
    }

    @Test
    void buscarCombinaFiltrosConAnd() {
        // Arrange
        var filtro = new GastoFiltroDto(
                HOY.minusDays(30), HOY, new BigDecimal("10.00"), null, Categoria.TRANSPORTE, "taxi");

        // Act
        var pagina = consultaGastoService.buscar(filtro, PAGINA);

        // Assert
        assertThat(ids(pagina)).containsExactly(ids(taxi));
    }

    @Test
    void buscarLimitaElTamanoDePaginaA50() {
        // Arrange
        var paginaGrande = PageRequest.of(0, 500);

        // Act
        var pagina = consultaGastoService.buscar(GastoFiltroDto.sinFiltros(), paginaGrande);
        var sinPaginar = consultaGastoService.buscar(GastoFiltroDto.sinFiltros(), Pageable.unpaged());

        // Assert
        assertThat(pagina.getSize()).isEqualTo(ConsultaGastoService.TAMANO_MAXIMO_PAGINA);
        assertThat(sinPaginar.getSize()).isEqualTo(ConsultaGastoService.TAMANO_MAXIMO_PAGINA);
    }

    @Test
    void buscarRechazaRangosYLimitesInvalidos() {
        // Arrange
        var fechasInvertidas = new GastoFiltroDto(HOY, HOY.minusDays(1), null, null, null, null);
        var importesInvertidos = new GastoFiltroDto(null, null, new BigDecimal("10"), new BigDecimal("5"), null, null);
        var minimoNegativo = new GastoFiltroDto(null, null, new BigDecimal("-1"), null, null, null);
        var maximoConTresDecimales = new GastoFiltroDto(null, null, null, new BigDecimal("1.001"), null, null);

        // Act and Assert
        assertThatThrownBy(() -> consultaGastoService.buscar(fechasInvertidas, PAGINA))
                .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> consultaGastoService.buscar(importesInvertidos, PAGINA))
                .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> consultaGastoService.buscar(minimoNegativo, PAGINA))
                .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> consultaGastoService.buscar(maximoConTresDecimales, PAGINA))
                .isInstanceOf(DatosInvalidosException.class);
    }
}
