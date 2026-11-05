package com.unir.gastos.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unir.gastos.dto.GastoFiltroDto;
import com.unir.gastos.dto.GastoResponseDto;
import com.unir.gastos.entity.Gasto;
import com.unir.gastos.exception.DatosInvalidosException;
import com.unir.gastos.exception.GastoNoEncontradoException;
import com.unir.gastos.repository.GastoRepository;
import com.unir.gastos.repository.GastoSpecifications;

@Service
@Transactional(readOnly = true)
public class ConsultaGastoServiceImpl implements ConsultaGastoService {

    private final GastoRepository gastoRepository;

    public ConsultaGastoServiceImpl(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    @Override
    public GastoResponseDto obtenerPorId(Long id) {
        if (id == null) {
            throw new DatosInvalidosException("El identificador del gasto es obligatorio");
        }
        return gastoRepository.findById(id)
                .map(GastoResponseDto::desde)
                .orElseThrow(() -> new GastoNoEncontradoException(id));
    }

    @Override
    public Page<GastoResponseDto> buscar(GastoFiltroDto filtro, Pageable pageable) {
        var filtroAplicado = filtro != null ? filtro : GastoFiltroDto.sinFiltros();
        validar(filtroAplicado);
        return gastoRepository.findAll(construirEspecificacion(filtroAplicado), limitarPagina(pageable))
                .map(GastoResponseDto::desde);
    }

    private void validar(GastoFiltroDto filtro) {
        if (filtro.fechaDesde() != null && filtro.fechaHasta() != null
                && filtro.fechaDesde().isAfter(filtro.fechaHasta())) {
            throw new DatosInvalidosException("fechaDesde no puede ser posterior a fechaHasta");
        }
        validarLimiteImporte(filtro.importeMinimo(), "importeMinimo");
        validarLimiteImporte(filtro.importeMaximo(), "importeMaximo");
        if (filtro.importeMinimo() != null && filtro.importeMaximo() != null
                && filtro.importeMinimo().compareTo(filtro.importeMaximo()) > 0) {
            throw new DatosInvalidosException("importeMinimo no puede ser mayor que importeMaximo");
        }
    }

    private void validarLimiteImporte(BigDecimal importe, String nombre) {
        if (importe == null) {
            return;
        }
        if (importe.signum() < 0) {
            throw new DatosInvalidosException(nombre + " no puede ser negativo");
        }
        if (importe.scale() > 2) {
            throw new DatosInvalidosException(nombre + " no puede tener mas de dos decimales");
        }
    }

    private Specification<Gasto> construirEspecificacion(GastoFiltroDto filtro) {
        List<Specification<Gasto>> especificaciones = new ArrayList<>();
        if (filtro.fechaDesde() != null) {
            especificaciones.add(GastoSpecifications.fechaDesde(filtro.fechaDesde()));
        }
        if (filtro.fechaHasta() != null) {
            especificaciones.add(GastoSpecifications.fechaHasta(filtro.fechaHasta()));
        }
        if (filtro.importeMinimo() != null) {
            especificaciones.add(GastoSpecifications.importeMinimo(filtro.importeMinimo()));
        }
        if (filtro.importeMaximo() != null) {
            especificaciones.add(GastoSpecifications.importeMaximo(filtro.importeMaximo()));
        }
        if (filtro.categoria() != null) {
            especificaciones.add(GastoSpecifications.categoria(filtro.categoria()));
        }
        if (filtro.descripcion() != null && !filtro.descripcion().isBlank()) {
            especificaciones.add(GastoSpecifications.descripcionContiene(filtro.descripcion().strip()));
        }
        return Specification.allOf(especificaciones);
    }

    private Pageable limitarPagina(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, TAMANO_MAXIMO_PAGINA);
        }
        if (pageable.getPageSize() > TAMANO_MAXIMO_PAGINA) {
            return PageRequest.of(pageable.getPageNumber(), TAMANO_MAXIMO_PAGINA, pageable.getSort());
        }
        return pageable;
    }
}
