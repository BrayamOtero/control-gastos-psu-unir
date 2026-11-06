import { useEffect, useState } from 'react';
import { listarGastos } from '../services/gastos.js';
import { filtrosMesActual } from '../services/filtrosGastos.js';

export function useListaGastos() {
  const [consulta, setConsulta] = useState(() => ({ filtros: filtrosMesActual(), pagina: 0 }));
  const [estado, setEstado] = useState({ cargando: true, error: '', resultado: null });

  useEffect(() => {
    const controlador = new AbortController();
    setEstado({ cargando: true, error: '', resultado: null });
    listarGastos(consulta.filtros, consulta.pagina, controlador.signal)
      .then((resultado) => {
        if (!controlador.signal.aborted) setEstado({ cargando: false, error: '', resultado });
      })
      .catch((error) => {
        // Las consultas sustituidas o desmontadas ya no deben actualizar la pantalla.
        if (!controlador.signal.aborted) setEstado({ cargando: false, error: error.message, resultado: null });
      });
    return () => controlador.abort();
  }, [consulta]);

  function aplicarFiltros(filtros) {
    setEstado({ cargando: true, error: '', resultado: null });
    setConsulta({ filtros, pagina: 0 });
  }

  function cambiarPagina(pagina) {
    setEstado({ cargando: true, error: '', resultado: null });
    setConsulta((actual) => ({ ...actual, pagina }));
  }

  function reintentar() {
    setConsulta((actual) => ({ ...actual }));
  }

  return { ...estado, filtros: consulta.filtros, aplicarFiltros, cambiarPagina, reintentar };
}
