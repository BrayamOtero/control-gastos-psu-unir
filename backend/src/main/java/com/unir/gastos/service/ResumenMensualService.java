package com.unir.gastos.service;

import com.unir.gastos.dto.ResumenMensualDto;

public interface ResumenMensualService {

    ResumenMensualDto obtener(int anio, int mes);
}
