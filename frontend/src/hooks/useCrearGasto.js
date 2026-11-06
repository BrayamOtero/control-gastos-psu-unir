import { useRef, useState } from 'react';
import { crearGasto } from '../services/gastos.js';

export function useCrearGasto() {
  const enCurso = useRef(false);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState('');
  const [gastoCreado, setGastoCreado] = useState(null);

  function limpiarMensajes() {
    setError('');
    setGastoCreado(null);
  }

  async function guardar(datos) {
    if (enCurso.current) return false;
    enCurso.current = true;
    setGuardando(true);
    limpiarMensajes();
    try {
      setGastoCreado(await crearGasto(datos));
      return true;
    } catch (error) {
      setError(error.message);
      return false;
    } finally {
      enCurso.current = false;
      setGuardando(false);
    }
  }

  return { guardar, guardando, error, gastoCreado, limpiarMensajes };
}
