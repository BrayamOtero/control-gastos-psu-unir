package com.unir.gastos.exception;

public class GastoNoEncontradoException extends RuntimeException {

    public GastoNoEncontradoException(Long id) {
        super("No existe el gasto con id " + id);
    }
}
