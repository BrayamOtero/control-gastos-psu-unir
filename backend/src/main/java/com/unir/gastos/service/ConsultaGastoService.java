package com.unir.gastos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.unir.gastos.dto.GastoFiltroDto;
import com.unir.gastos.dto.GastoResponseDto;

public interface ConsultaGastoService {

    int TAMANO_MAXIMO_PAGINA = 50;

    GastoResponseDto obtenerPorId(Long id);

    Page<GastoResponseDto> buscar(GastoFiltroDto filtro, Pageable pageable);
}
