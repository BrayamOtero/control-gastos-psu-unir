import { useEffect, useState } from 'react';
import { obtenerResumenMensual } from '../services/gastos.js';
import { periodoActual } from '../services/resumenMensual.js';

export function useResumenMensual() {
  const [periodo, setPeriodo] = useState(periodoActual);
  const [estado, setEstado] = useState({ cargando: true, error: '', resumen: null });
  const [intento, setIntento] = useState(0);

  useEffect(() => {
    const controlador = new AbortController();
    setEstado({ cargando: true, error: '', resumen: null });
    obtenerResumenMensual(periodo, controlador.signal)
      .then((resumen) => {
        if (!controlador.signal.aborted) setEstado({ cargando: false, error: '', resumen });
      })
      .catch((error) => {
        if (!controlador.signal.aborted) setEstado({ cargando: false, error: error.message, resumen: null });
      });
    return () => controlador.abort();
  }, [periodo, intento]);

  return {
    ...estado,
    periodo,
    consultar: setPeriodo,
    reintentar: () => setIntento((actual) => actual + 1),
  };
}
