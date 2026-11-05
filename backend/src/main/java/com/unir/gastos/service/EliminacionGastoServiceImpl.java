package com.unir.gastos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unir.gastos.exception.DatosInvalidosException;
import com.unir.gastos.exception.GastoNoEncontradoException;
import com.unir.gastos.repository.GastoRepository;

@Service
@Transactional
public class EliminacionGastoServiceImpl implements EliminacionGastoService {

    private final GastoRepository gastoRepository;

    public EliminacionGastoServiceImpl(GastoRepository gastoRepository) {
        this.gastoRepository = gastoRepository;
    }

    @Override
    public void eliminar(Long id) {
        if (id == null || id <= 0) {
            throw new DatosInvalidosException("El identificador del gasto debe ser positivo");
        }
        var gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new GastoNoEncontradoException(id));
        gastoRepository.delete(gasto);
    }
}
