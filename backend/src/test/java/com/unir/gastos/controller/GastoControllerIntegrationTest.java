package com.unir.gastos.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.unir.gastos.dto.GastoRequestDto;
import com.unir.gastos.dto.GastoResponseDto;
import com.unir.gastos.dto.ResumenCategoriaDto;
import com.unir.gastos.dto.ResumenMensualDto;
import com.unir.gastos.entity.Categoria;
import com.unir.gastos.repository.GastoRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:file:./build/test-data/gastos")
class GastoControllerIntegrationTest {

    private static final LocalDate HOY = LocalDate.now();

    record Error(int status, String title, String detail) {
    }

    record Metadatos(int size, int number, long totalElements, int totalPages) {
    }

    record Pagina(List<GastoResponseDto> content, Metadatos page) {
    }

    @Value("${local.server.port}")
    private int puerto;

    @Autowired
    private GastoRepository gastoRepository;

    private RestClient cliente;

    @BeforeEach
    void setUp() {
        gastoRepository.deleteAll();
        cliente = RestClient.builder()
                .baseUrl("http://localhost:" + puerto + "/api/gastos")
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> { })
                .build();
    }

    private GastoResponseDto crear(LocalDate fecha, String importe, Categoria categoria, String descripcion) {
        return cliente.post()
                .body(new GastoRequestDto(fecha, new BigDecimal(importe), categoria, descripcion))
                .retrieve()
                .body(GastoResponseDto.class);
    }

    private ResponseEntity<Error> postJson(String json) {
        return cliente.post().contentType(MediaType.APPLICATION_JSON).body(json).retrieve().toEntity(Error.class);
    }

    private ResponseEntity<Pagina> listar(String query) {
        return cliente.get().uri(query).retrieve().toEntity(Pagina.class);
    }

    private ResponseEntity<Error> listarConError(String query) {
        return cliente.get().uri(query).retrieve().toEntity(Error.class);
    }

    private static List<Long> ids(ResponseEntity<Pagina> respuesta) {
        return respuesta.getBody().content().stream().map(GastoResponseDto::id).toList();
    }

    // ---------- Crear ----------

    @Test
    void crearDevuelve201ConLocationYElGasto() {
        // Arrange
        var request = new GastoRequestDto(HOY, new BigDecimal("12.50"), Categoria.ALIMENTACION, "Pan");

        // Act
        var respuesta = cliente.post().body(request).retrieve().toEntity(GastoResponseDto.class);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var cuerpo = respuesta.getBody();
        assertThat(cuerpo.id()).isNotNull();
        assertThat(cuerpo.fecha()).isEqualTo(HOY);
        assertThat(cuerpo.importe()).isEqualByComparingTo("12.50");
        assertThat(cuerpo.categoria()).isEqualTo(Categoria.ALIMENTACION);
        assertThat(cuerpo.descripcion()).isEqualTo("Pan");
        assertThat(respuesta.getHeaders().getLocation()).hasPath("/api/gastos/" + cuerpo.id());
        assertThat(gastoRepository.existsById(cuerpo.id())).isTrue();
    }

    @Test
    void crearConDatosInvalidosDevuelve400() {
        // Arrange
        var manana = HOY.plusDays(1);
        var cuerpos = List.of(
                "{\"importe\": 1.00, \"categoria\": \"OCIO\"}",
                "{\"fecha\": \"" + manana + "\", \"importe\": 1.00, \"categoria\": \"OCIO\"}",
                "{\"fecha\": \"" + HOY + "\", \"categoria\": \"OCIO\"}",
                "{\"fecha\": \"" + HOY + "\", \"importe\": 0, \"categoria\": \"OCIO\"}",
                "{\"fecha\": \"" + HOY + "\", \"importe\": 1.001, \"categoria\": \"OCIO\"}",
                "{\"fecha\": \"" + HOY + "\", \"importe\": 1.00}",
                "{\"fecha\": \"" + HOY + "\", \"importe\": 1.00, \"categoria\": \"OCIO\", \"descripcion\": \""
                        + "x".repeat(256) + "\"}");

        // Act and Assert
        for (var json : cuerpos) {
            var respuesta = postJson(json);
            assertThat(respuesta.getStatusCode()).as(json).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(respuesta.getBody().detail()).as(json).isNotBlank();
        }
        assertThat(gastoRepository.count()).isZero();
    }

    @Test
    void crearConJsonMalFormadoOCategoriaDesconocidaDevuelve400() {
        // Act
        var categoriaDesconocida = postJson(
                "{\"fecha\": \"" + HOY + "\", \"importe\": 1.00, \"categoria\": \"VIAJES\"}");
        var jsonRoto = postJson("{\"fecha\": ");

        // Assert
        assertThat(categoriaDesconocida.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(jsonRoto.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ---------- Consultar por id ----------

    @Test
    void obtenerPorIdDevuelve200() {
        // Arrange
        var creado = crear(HOY, "9.99", Categoria.OCIO, "Cine");

        // Act
        var respuesta = cliente.get().uri("/{id}", creado.id()).retrieve().toEntity(GastoResponseDto.class);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(creado);
    }

    @Test
    void obtenerPorIdInexistenteDevuelve404() {
        // Act
        var respuesta = cliente.get().uri("/{id}", Long.MAX_VALUE).retrieve().toEntity(Error.class);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody().status()).isEqualTo(404);
        assertThat(respuesta.getBody().detail()).contains(String.valueOf(Long.MAX_VALUE));
    }

    @Test
    void obtenerPorIdNoNumericoDevuelve400() {
        // Act
        var respuesta = cliente.get().uri("/abc").retrieve().toEntity(Error.class);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ---------- Buscar ----------

    @Test
    void buscarSinFiltrosDevuelveTodosPaginados() {
        // Arrange
        var a = crear(HOY.minusDays(2), "10.00", Categoria.OCIO, null);
        var b = crear(HOY, "20.00", Categoria.SALUD, null);

        // Act
        var respuesta = listar("?size=1");

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ids(respuesta)).containsExactly(a.id());
        assertThat(respuesta.getBody().page()).isEqualTo(new Metadatos(1, 0, 2, 2));
        assertThat(ids(listar(""))).containsExactly(a.id(), b.id());
    }

    @Test
    void buscarCombinaFiltrosConAnd() {
        // Arrange
        crear(HOY.minusDays(10), "50.00", Categoria.ALIMENTACION, "Compra Supermercado");
        var taxi = crear(HOY.minusDays(5), "15.50", Categoria.TRANSPORTE, "Taxi al aeropuerto");
        crear(HOY, "15.50", Categoria.TRANSPORTE, "Autobus");
        var query = "?fechaDesde=" + HOY.minusDays(6) + "&fechaHasta=" + HOY.minusDays(5)
                + "&importeMinimo=15.50&importeMaximo=15.50&categoria=TRANSPORTE&descripcion= TAXI ";

        // Act
        var respuesta = listar(query);

        // Assert
        assertThat(ids(respuesta)).containsExactly(taxi.id());
    }

    @Test
    void buscarLimitaElTamanoDePaginaA50() {
        // Act
        var respuesta = listar("?size=500");

        // Assert
        assertThat(respuesta.getBody().page().size()).isEqualTo(50);
    }

    @Test
    void buscarConFiltrosInvalidosDevuelve400() {
        // Arrange
        var consultas = List.of(
                "?fechaDesde=" + HOY + "&fechaHasta=" + HOY.minusDays(1),
                "?importeMinimo=10&importeMaximo=5",
                "?importeMinimo=-1",
                "?importeMaximo=1.001",
                "?fechaDesde=04-11-2026",
                "?importeMinimo=abc",
                "?categoria=VIAJES",
                "?sort=inexistente");

        // Act and Assert
        for (var query : consultas) {
            assertThat(listarConError(query).getStatusCode()).as(query).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // ---------- Modificar ----------

    @Test
    void modificarReemplazaLosCamposYConservaElId() {
        // Arrange
        var creado = crear(HOY.minusDays(1), "10.00", Categoria.OCIO, "Cine");
        var request = new GastoRequestDto(HOY, new BigDecimal("80.25"), Categoria.VIVIENDA, null);

        // Act
        var respuesta = cliente.put().uri("/{id}", creado.id()).body(request)
                .retrieve().toEntity(GastoResponseDto.class);

        // Assert
        var esperado = new GastoResponseDto(creado.id(), HOY, new BigDecimal("80.25"), Categoria.VIVIENDA, null);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(esperado);
        assertThat(GastoResponseDto.desde(gastoRepository.findById(creado.id()).orElseThrow()))
                .usingRecursiveComparison().withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(esperado);
    }

    @Test
    void modificarInexistenteDevuelve404() {
        // Arrange
        var request = new GastoRequestDto(HOY, BigDecimal.ONE, Categoria.OTROS, null);

        // Act
        var respuesta = cliente.put().uri("/{id}", Long.MAX_VALUE).body(request).retrieve().toEntity(Error.class);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void modificarConDatosOIdInvalidosDevuelve400() {
        // Arrange
        var creado = crear(HOY, "10.00", Categoria.OCIO, "Cine");
        var invalido = new GastoRequestDto(HOY.plusDays(1), new BigDecimal("1.00"), Categoria.OCIO, null);
        var valido = new GastoRequestDto(HOY, BigDecimal.ONE, Categoria.OTROS, null);

        // Act
        var datosInvalidos = cliente.put().uri("/{id}", creado.id()).body(invalido)
                .retrieve().toEntity(Error.class);
        var idNoPositivo = cliente.put().uri("/{id}", 0).body(valido).retrieve().toEntity(Error.class);

        // Assert
        assertThat(datosInvalidos.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(idNoPositivo.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(gastoRepository.findById(creado.id()).orElseThrow().getDescripcion()).isEqualTo("Cine");
    }

    // ---------- Eliminar ----------

    @Test
    void eliminarDevuelve204YElGastoDejaDeExistir() {
        // Arrange
        var creado = crear(HOY, "5.00", Categoria.SERVICIOS, null);

        // Act
        var respuesta = cliente.delete().uri("/{id}", creado.id()).retrieve().toBodilessEntity();

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(cliente.get().uri("/{id}", creado.id()).retrieve().toBodilessEntity().getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void eliminarInexistenteDevuelve404YIdNoPositivo400() {
        // Act
        var inexistente = cliente.delete().uri("/{id}", Long.MAX_VALUE).retrieve().toEntity(Error.class);
        var noPositivo = cliente.delete().uri("/{id}", -1).retrieve().toEntity(Error.class);

        // Assert
        assertThat(inexistente.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(noPositivo.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ---------- Resumen mensual ----------

    @Test
    void resumenDevuelve200ConTotalesPorcentajesYOrdenPorImporte() {
        // Arrange
        var mes = YearMonth.from(HOY).minusMonths(1);
        crear(mes.atDay(1), "30.00", Categoria.VIVIENDA, null);
        crear(mes.atEndOfMonth(), "10.00", Categoria.OCIO, null);
        crear(mes.atEndOfMonth().plusDays(1), "99.00", Categoria.OCIO, null);

        // Act
        var respuesta = cliente.get()
                .uri("/resumen?mes={mes}&anio={anio}", mes.getMonthValue(), mes.getYear())
                .retrieve().toEntity(ResumenMensualDto.class);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        var resumen = respuesta.getBody();
        assertThat(resumen.anio()).isEqualTo(mes.getYear());
        assertThat(resumen.mes()).isEqualTo(mes.getMonthValue());
        assertThat(resumen.totalGastado()).isEqualByComparingTo("40.00");
        assertThat(resumen.numeroGastos()).isEqualTo(2);
        assertThat(resumen.categorias()).hasSize(Categoria.values().length);
        assertThat(resumen.categorias()).extracting(ResumenCategoriaDto::categoria)
                .startsWith(Categoria.VIVIENDA, Categoria.OCIO);
        assertThat(resumen.categorias().get(0).porcentaje()).isEqualByComparingTo("75.00");
        assertThat(resumen.categorias().get(1).porcentaje()).isEqualByComparingTo("25.00");
        assertThat(resumen.categorias().get(2).total()).isEqualByComparingTo("0");
    }

    @Test
    void resumenDeMesSinGastosDevuelve200ConCeros() {
        // Act
        var respuesta = cliente.get().uri("/resumen?mes=1&anio=2000").retrieve().toEntity(ResumenMensualDto.class);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().totalGastado()).isEqualByComparingTo("0");
        assertThat(respuesta.getBody().numeroGastos()).isZero();
        assertThat(respuesta.getBody().categorias()).hasSize(Categoria.values().length);
    }

    @Test
    void resumenConParametrosInvalidosDevuelve400() {
        // Arrange
        var consultas = List.of(
                "/resumen?mes=13&anio=2026",
                "/resumen?mes=0&anio=2026",
                "/resumen?mes=5&anio=0",
                "/resumen?mes=5",
                "/resumen?anio=2026",
                "/resumen?mes=abc&anio=2026");

        // Act and Assert
        for (var uri : consultas) {
            assertThat(cliente.get().uri(uri).retrieve().toEntity(Error.class).getStatusCode())
                    .as(uri).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
