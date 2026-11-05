package com.unir.gastos.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.unir.gastos.exception.DatosInvalidosException;
import com.unir.gastos.exception.GastoNoEncontradoException;

// Hereda el tratamiento 400 de errores de formato (JSON ilegible, tipos de parámetros) en formato ProblemDetail.
@RestControllerAdvice
public class ManejadorGlobalExcepciones extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DatosInvalidosException.class)
    public ProblemDetail manejarDatosInvalidos(DatosInvalidosException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(GastoNoEncontradoException.class)
    public ProblemDetail manejarNoEncontrado(GastoNoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}
