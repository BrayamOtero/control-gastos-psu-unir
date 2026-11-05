package com.unir.gastos.service;

import com.unir.gastos.dto.GastoRequestDto;
import com.unir.gastos.dto.GastoResponseDto;

public interface CreacionGastoService {

    GastoResponseDto crear(GastoRequestDto request);
}
