package com.unir.gastos.service;

import com.unir.gastos.dto.GastoRequestDto;
import com.unir.gastos.dto.GastoResponseDto;

public interface ModificacionGastoService {

    GastoResponseDto modificar(Long id, GastoRequestDto request);
}
