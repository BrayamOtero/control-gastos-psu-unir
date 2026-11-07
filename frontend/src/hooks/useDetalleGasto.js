import { useEffect, useState } from 'react';
import { obtenerGasto } from '../services/gastos.js';

export function useDetalleGasto(id) {
  const [estado, setEstado] = useState({ cargando: true, error: '', noEncontrado: false, gasto: null });
  const [intento, setIntento] = useState(0);

  useEffect(() => {
    const controlador = new AbortController();
    setEstado({ cargando: true, error: '', noEncontrado: false, gasto: null });
    obtenerGasto(id, controlador.signal)
      .then((gasto) => {
        if (!controlador.signal.aborted) setEstado({ cargando: false, error: '', noEncontrado: false, gasto });
      })
      .catch((error) => {
        if (!controlador.signal.aborted) {
          setEstado({ cargando: false, error: error.message, noEncontrado: error.status === 404, gasto: null });
        }
      });
    return () => controlador.abort();
  }, [id, intento]);

  function reintentar() {
    setIntento((actual) => actual + 1);
  }

  function actualizar(gasto) {
    setEstado((actual) => ({ ...actual, gasto }));
  }

  return { ...estado, reintentar, actualizar };
}
