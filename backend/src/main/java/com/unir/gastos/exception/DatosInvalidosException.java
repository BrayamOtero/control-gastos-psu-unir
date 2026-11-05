package com.unir.gastos.exception;

public class DatosInvalidosException extends IllegalArgumentException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
