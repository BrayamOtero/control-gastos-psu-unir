import { useEffect, useState } from 'react';
import { listarGastos } from '../services/gastos.js';

// La consulta (filtros y página) la conserva App para mantenerla al volver del detalle.
export function useListaGastos(consulta, setConsulta) {
  const [estado, setEstado] = useState({ cargando: true, error: '', resultado: null });
  const [intento, setIntento] = useState(0);

  useEffect(() => {
    const controlador = new AbortController();
    setEstado({ cargando: true, error: '', resultado: null });
    listarGastos(consulta.filtros, consulta.pagina, controlador.signal)
      .then((resultado) => {
        if (controlador.signal.aborted) return;
        const { number, totalPages } = resultado.page;
        // Tras eliminar, la página actual puede dejar de existir: se muestra la última disponible.
        if (number > 0 && number >= totalPages) {
          setConsulta((actual) => ({ ...actual, pagina: Math.max(totalPages - 1, 0) }));
          return;
        }
        setEstado({ cargando: false, error: '', resultado });
      })
      .catch((error) => {
        if (!controlador.signal.aborted) setEstado({ cargando: false, error: error.message, resultado: null });
      });
    return () => controlador.abort();
  }, [consulta, intento, setConsulta]);

  function aplicarFiltros(filtros) {
    setConsulta({ filtros, pagina: 0 });
  }

  function cambiarPagina(pagina) {
    setConsulta((actual) => ({ ...actual, pagina }));
  }

  function recargar() {
    setIntento((actual) => actual + 1);
  }

  return { ...estado, aplicarFiltros, cambiarPagina, recargar };
}
