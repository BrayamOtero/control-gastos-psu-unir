package com.unir.gastos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unir.gastos.dto.GastoRequestDto;
import com.unir.gastos.dto.GastoResponseDto;
import com.unir.gastos.entity.Gasto;
import com.unir.gastos.exception.DatosInvalidosException;
import com.unir.gastos.exception.GastoNoEncontradoException;
import com.unir.gastos.repository.GastoRepository;

@Service
@Transactional
public class ModificacionGastoServiceImpl implements ModificacionGastoService {

    private final GastoRepository gastoRepository;

    public ModificacionGastoServiceImpl(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    @Override
    public GastoResponseDto modificar(Long id, GastoRequestDto request) {
        if (id == null || id <= 0) {
            throw new DatosInvalidosException("El identificador del gasto debe ser positivo");
        }
        if (request == null) {
            throw new DatosInvalidosException("Los datos del gasto son obligatorios");
        }
        // Se valida en un objeto aparte para no dejar la entidad gestionada a medio modificar.
        var valores = new Gasto(request.fecha(), request.importe(), request.categoria(), request.descripcion());
        var gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new GastoNoEncontradoException(id));
        gasto.setFecha(valores.getFecha());
        gasto.setImporte(valores.getImporte());
        gasto.setCategoria(valores.getCategoria());
        gasto.setDescripcion(valores.getDescripcion());
        return GastoResponseDto.desde(gastoRepository.save(gasto));
    }
}
