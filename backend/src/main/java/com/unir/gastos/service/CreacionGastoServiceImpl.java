package com.unir.gastos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unir.gastos.dto.GastoRequestDto;
import com.unir.gastos.dto.GastoResponseDto;
import com.unir.gastos.entity.Gasto;
import com.unir.gastos.exception.DatosInvalidosException;
import com.unir.gastos.repository.GastoRepository;

@Service
@Transactional
public class CreacionGastoServiceImpl implements CreacionGastoService {

    private final GastoRepository gastoRepository;

    public CreacionGastoServiceImpl(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    @Override
    public GastoResponseDto crear(GastoRequestDto request) {
        if (request == null) {
            throw new DatosInvalidosException("Los datos del gasto son obligatorios");
        }
        var gasto = new Gasto(request.fecha(), request.importe(), request.categoria(), request.descripcion());
        return GastoResponseDto.desde(gastoRepository.save(gasto));
    }
}
